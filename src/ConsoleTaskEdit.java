import java.util.Date;

public class ConsoleTaskEdit extends ConsoleDialogue {
    public TodoList dataBase;
    public int taskId;
    public ConsoleTaskEdit(StringBuilder out, TodoList dataBase, int taskId) {
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
            case 3: out.append("Срок: "); break;
        }
    }

    @Override
    public void submitAnswer(int id, String line, StringBuilder out) throws Exception {
        if(line.equals("-"))
            return;

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
                answers.put(id, TodoListConsoleController.defaultDateParser.parse(line));
                break;
        }
    }

    @Override
    public void finish(StringBuilder out) {
        var task = dataBase.tasks.get(taskId);
        if(answers.containsKey(0)) task.title = (String) answers.get(0);
        if(answers.containsKey(1)) task.description = (String) answers.get(1);
        if(answers.containsKey(2)) task.priority = (int) answers.get(2);
        if(answers.containsKey(3)) task.deadline = (Date) answers.get(3);
        out.append("Задача #").append(taskId).append(" была успешно отредактирована\n");

        try { dataBase.saveChanges(); }
        catch (Exception e) { out.append("но изменения не могут быть сохранены на диск:\n").append(e).append('\n'); }
    }
}
