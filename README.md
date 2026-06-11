# Student Grade Tracker

An elegant, single-view web application designed for educators, instructors, and students to track academic grades, compute statistics, and visualize student performance distributions.

This app features dynamic computations, secure in-browser storage, and a responsive responsive interface styled with Tailwind CSS.

---

## Features

- **Class Performance Overview**: Real-time tracking of Class Average, Highest Score (pinned to the top student), Lowest Score, and Total Registered Students.
- **Dynamic Grade Calculations**: Automatic assignment of letter grades (A, B, C, D, F) and performance labels (Excellent, Good, Average, Passing, Poor).
- **Responsive Management Table**: Instantly add, edit, or delete student records with a fully reactive tabular list.
- **Preserved State**: Your data remains safely synchronized locally in the browser using the browser's persistent `localStorage`.
- **Prepopulated Presets**: Quick-load preset student directories featuring realistic demo values with Indian names for faster testing.
- **Score Distribution Visualizer**: Dynamic visual bar graphs displaying the real-time distribution of letter grades among the class roster.



## Getting Started (Local Setup in VS Code)

Follow these step-by-step instructions to set up and run the application in Visual Studio Code:

### 1. Prerequisites
Ensure you have the following software installed on your machine:
- [Visual Studio Code](https://code.visualstudio.com/)

### 2. Download or Clone the Files
Place the project files inside a dedicated folder of your choice (e.g., `student-grade-tracker`).

### 3. Open the Folder in VS Code
1. Open VS Code.
2. Go to **File** > **Open Folder...**
3. Select your project folder and click **Open**.

### 4. Install Dependencies
Open a new integrated terminal in VS Code (**Terminal** > **New Terminal** or press ``Ctrl + ` ``) and run:
```bash
npm install
```

### 5. Start the Development Server
To launch the application locally in development mode:
```bash
npm run dev
```
Once started, the terminal will display a local address (usually `http://localhost:3000`). Ctrl-click or paste this link into your preferred browser to interact with the application.


```

