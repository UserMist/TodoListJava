package main.todolist.console;
import main.todolist.*;
import main.*;

import java.util.Date;

public class DialogueNew extends ConsoleDialogue {
    private final TodoList dataBase;
    private final int taskId;

    public DialogueNew(StringBuilder out, TodoList dataBase, int taskId) {
        super(out);
        this.dataBase = dataBase;
        this.taskId = taskId;
    }

    @Override
    public int getQuestionAmount() {
        return 4;
    }

    @Override
    public void printQuestion(int id, StringBuilder out) {
        switch(id) {
            case 0: out.append("Заголовок: "); break;
            case 1: out.append("Описание: "); break;
            case 2: out.append("Важность: "); break;
            case 3: out.append("Срок (yyyy-mm-dd): "); break;
        }
    }

    @Override
    public void submitAnswer(int id, String line, StringBuilder out) throws Exception {
        switch(id) {
            case 0:
                if(line.length() > 50) {
                    answers.put(id, line.substring(0, 50));
                    out.append("Заголовок был урезан до 50 символов\n");
                }
                else {
                    answers.put(id, line);
                }
                break;
            case 1:
                answers.put(id, line);
                break;
            case 2:
                var priority = Integer.parseInt(line);
                if(priority < 0 || priority > 10)
                    throw new Exception("Число лежать в интервале [0; 10]");
                answers.put(id, priority);
                break;
            case 3:
                answers.put(id, Controller.defaultDateParser.parse(line));
                break;
        }
    }

    @Override
    public void finish(StringBuilder out) {
        dataBase.createTask(taskId, (String) answers.get(0), (String) answers.get(1), (int) answers.get(2), (Date) answers.get(3));
        out.append("Задача #").append(taskId).append(" была успешно создана\n");
        try { dataBase.saveChanges(); }
        catch (Exception e) { out.append("но изменения не могут быть сохранены на диск:\n").append(e).append('\n'); }
    }
}
