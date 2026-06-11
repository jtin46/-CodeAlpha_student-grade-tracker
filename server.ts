import express from "express";
import path from "path";
import fs from "fs";
import { spawn, exec, ChildProcess } from "child_process";
import { createServer as createViteServer } from "vite";
import { fileURLToPath } from "url";

const app = express();
const PORT = 3000;

app.use(express.json());

// In-memory active Java terminal sessions
interface Session {
  child: ChildProcess;
  outputBuffer: string;
  lastActive: number;
}
const sessions = new Map<string, Session>();

// Session Scavenger - check for inactive sessions every 30 seconds
setInterval(() => {
  const now = Date.now();
  for (const [id, session] of sessions.entries()) {
    // Kill processes inactive for more than 5 minutes
    if (now - session.lastActive > 300000) {
      console.log(`[Scavenger] Cleaning up inactive Java session: ${id}`);
      try {
        session.child.kill();
      } catch (err) {
        // ignore
      }
      sessions.delete(id);
    }
  }
}, 30000);

// API 1: Read the Java code
app.get("/api/code/read", (req, res) => {
  try {
    const javaFilePath = path.join(process.cwd(), "StudentGradeTracker.java");
    if (fs.existsSync(javaFilePath)) {
      const code = fs.readFileSync(javaFilePath, "utf-8");
      res.json({ success: true, code });
    } else {
      res.status(404).json({ success: false, error: "StudentGradeTracker.java not found." });
    }
  } catch (error: any) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// API 2: Save updated Java code
app.post("/api/code/write", (req, res) => {
  const { code } = req.body;
  if (!code) {
    return res.status(400).json({ success: false, error: "No code provided." });
  }

  // Basic security filtering to prevent malicious runtime commands inside our sandbox
  const bannedKeywords = ["System.exit", "Runtime.getRuntime", "ProcessBuilder", "FileOutputStream", "FileWriter", "Files.write"];
  for (const keyword of bannedKeywords) {
    if (code.includes(keyword)) {
      return res.status(400).json({
        success: false,
        error: `Security Check: Usage of '${keyword}' is disabled in this secure classroom console.`
      });
    }
  }

  try {
    const javaFilePath = path.join(process.cwd(), "StudentGradeTracker.java");
    fs.writeFileSync(javaFilePath, code, "utf-8");
    res.json({ success: true, message: "Code saved successfully." });
  } catch (error: any) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// API 3: Compile and start a stateful interactive terminal session
app.post("/api/session/start", (req, res) => {
  const sessionId = Math.random().toString(36).substring(2, 15);
  const javaFilePath = path.join(process.cwd(), "StudentGradeTracker.java");

  console.log(`[Session] Requested compile & start for session: ${sessionId}`);

  // Step A: Check if java compilable and compile first
  exec("javac StudentGradeTracker.java", (compileError, stdout, stderr) => {
    if (compileError || stderr) {
      console.error(`[Compiler Error]: ${stderr || stdout}`);
      return res.json({
        success: false,
        source: "compiler",
        error: stderr || stdout || compileError?.message
      });
    }

    try {
      // Step B: Spawn Java Class
      const child = spawn("java", ["StudentGradeTracker"], {
        cwd: process.cwd(),
        env: { ...process.env, LANG: "en_US.UTF-8" }
      });

      let outputBuffer = "";

      // Capture stdout
      child.stdout?.on("data", (data) => {
        outputBuffer += data.toString();
      });

      // Capture stderr
      child.stderr?.on("data", (data) => {
        outputBuffer += data.toString();
      });

      child.on("close", (code) => {
        console.log(`[Session] Java finished with exit code ${code}`);
        outputBuffer += `\n\n[Process completed with exit code ${code}]`;
      });

      child.on("error", (err) => {
        outputBuffer += `\n\n[Process system runtime error: ${err.message}]`;
      });

      // Save session
      sessions.set(sessionId, {
        child,
        outputBuffer,
        lastActive: Date.now()
      });

      res.json({
        success: true,
        sessionId,
        message: "JVM online and running StudentGradeTracker."
      });

    } catch (launchError: any) {
      console.error(`[JVM Spawn Failure]: ${launchError.message}`);
      res.json({
        success: false,
        source: "runtime_spawn",
        error: `Failed to spawn JVM: ${launchError.message}`
      });
    }
  });
});

// API 4: Poll Output
app.get("/api/session/output", (req, res) => {
  const sessionId = req.query.sessionId as string;
  if (!sessionId) {
    return res.status(400).json({ error: "No sessionId provided." });
  }

  const session = sessions.get(sessionId);
  if (!session) {
    return res.status(404).json({ error: "Session expired or not found." });
  }

  // Update activity timestamp
  session.lastActive = Date.now();

  const data = session.outputBuffer;
  // Clear the buffer after sending (so client only gets incremental updates)
  session.outputBuffer = "";

  // Check if process has exited
  const isDead = session.child.killed || session.child.exitCode !== null;

  res.json({
    success: true,
    data,
    isEnded: isDead
  });

  if (isDead) {
    sessions.delete(sessionId);
  }
});

// API 5: Write Input
app.post("/api/session/input", (req, res) => {
  const { sessionId, input } = req.body;
  if (!sessionId) {
    return res.status(400).json({ error: "No sessionId provided." });
  }

  const session = sessions.get(sessionId);
  if (!session) {
    return res.status(404).json({ error: "Active Java session not found." });
  }

  session.lastActive = Date.now();

  // Write input into stdin of Java
  try {
    if (session.child && session.child.stdin) {
      session.child.stdin.write(input + "\n");
    } else {
      return res.status(400).json({ error: "Process input channel closed." });
    }
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// API 6: Stop Session
app.post("/api/session/stop", (req, res) => {
  const { sessionId } = req.body;
  if (!sessionId) {
    return res.status(400).json({ error: "No sessionId." });
  }

  const session = sessions.get(sessionId);
  if (session) {
    try {
      session.child.kill("SIGTERM");
    } catch (e) {
      // ignore
    }
    sessions.delete(sessionId);
    res.json({ success: true, message: "JVM terminated." });
  } else {
    res.status(404).json({ error: "Session not active." });
  }
});

// Start server
async function bootServer() {
  // Vite integration
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa"
    });
    app.use(vite.middlewares);
  } else {
    // Serve production built dist assets
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server executing at http://localhost:${PORT}`);
  });
}

bootServer().catch((err) => {
  console.error("Boot failure:", err);
});
