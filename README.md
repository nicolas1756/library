**IF on vs code**

ensure your launch.json looks something like this:

{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Launch Library",
      "request": "launch",
      "mainClass": "com.mycompany.library.Library",
      "cwd": "${workspaceFolder}/library"
    }
  ]
}

==============================================================

**In the event of file corruption**

1. search for Library.java
2. Uncomment this line around line 17:
   //Uncomment to fix book data file
   //consoleUtil util = new consoleUtil();
   //util.loadBooks();
