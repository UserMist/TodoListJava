import java.util.*;
public class TodoListConsoleController extends ConsoleController {
    public ConsoleController redirection = null;
    public TodoList dataBase;

    public TodoListConsoleController(TodoList dataBase) {
        this.dataBase = dataBase;
    }

    private ArrayList<String> parseCmdWords(String cmdLine) {
        var lineLen = cmdLine.length();
        var words = new ArrayList<String>();
        var wordOpener = '?';
        var wordStart = -1;

        for(int i = 0; i < lineLen; i++) {
            var ch = cmdLine.charAt(i);
            if(wordOpener != '?') {
                if(ch == wordOpener) {
                    words.add(cmdLine.substring(wordStart, i));
                    wordOpener = '?';
                }

                if(wordOpener != ' ' && ch == '\\' && i+1 < lineLen)
                    i++;
            }
            else if(ch == '"' || ch == '\'') {
                wordOpener = ch;
                wordStart = i+1;
            }
            else if(ch != ' ') {
                wordOpener = ' ';
                wordStart = i;
            }
        }

        if(wordOpener != '?')
            words.add(cmdLine.substring(wordStart));

        return words;
    }

    private static final HashMap<String, String> cmdDescriptions = new HashMap<>() {{
        put("help", "              Перечислить консольные команды");
        put("list", "              Перечислить задачи");
        put("new", "               Создать задачу");
        put("remove", "            Удалить задачу");
        put("complete", "          Завершить задачу");
        put("edit", "              Редактировать задачу");
    }};

    private void executeHelp(ArrayList<String> words, StringBuilder out) {
        if(words.size() == 1) {
            cmdDescriptions.forEach((k,v) -> out.append(k).append(v).append('\n'));
            return;
        }

        if(cmdDescriptions.containsKey(words.get(1))) {
            var arg = words.get(1);
            out.append(arg).append(cmdDescriptions.get(arg)).append('\n');
            switch(words.get(1))
            {
                case "list":
                    out.append(" -s new           Только новые\n");
                    out.append(" -s in_progress   Только текущие\n");
                    out.append(" -s done          Только завершенные\n");
                    break;
                case "help":
                    out.append("help [<команда>]  Помощь по команде\n");
                    break;
                case "new":
                    out.append("new <идентиф.>\n");
                    break;
                case "remove":
                    out.append("remove <идентиф.>\n");
                    break;
                case "edit":
                    out.append("edit <идентиф.>\n");
                    out.append("Примечание:  Прочерк (-) оставляет поле без изменений\n");
                    break;
                default:
                    break;
            }
        }
        else {
            out.append("Помощь по команде ").append(words.get(1)).append(" не найдена\n");
        }
    }

    private void executeList(ArrayList<String> words, StringBuilder out) {
        ArrayList<HashMap.Entry<Integer, TodoTask>> taskView = null;
        if(words.size() == 1) {
            taskView = dataBase.getTaskView();
        }
        else if(words.size() == 3 && words.get(1).equals("-s")) {
            var status = TodoTask.Status.Invalid;
            switch(words.get(2)) {
                case "new": status = TodoTask.Status.New; break;
                case "in_progress": status = TodoTask.Status.InProgress; break;
                case "done": status = TodoTask.Status.Done; break;
                default: break;
            }
            taskView = dataBase.getTaskView(status);
        }
        else {
            out.append("Команда указана неверно\n");
            return;
        }

        taskView.sort(Comparator.comparingInt(a -> -a.getValue().priority));
        taskView.forEach(pair -> {
            var id = pair.getKey();
            var task = pair.getValue();

            var statusWord = "приоритет "+task.priority+", ";
            switch(task.status) {
                case New: statusWord += "новая"; break;
                case InProgress: statusWord += "текущая"; break;
                case Done: statusWord += "завершена " + task.completionDate; break;
                default: statusWord += "invalid"; break;
            }
            out.append('[').append(id).append("] ").append(task.title).append(" (").append(statusWord).append(')');
            out.append('\n');
        });

        if(taskView.isEmpty())
            out.append("Задачи не найдены\n");
    }
    private void executeNew(ArrayList<String> words, StringBuilder out) {
        try {
            if(words.size() == 1) {
                out.append("Необходимо сперва указать идентификатор\n");
                return;
            }

            var taskId = Integer.parseInt(words.get(1));
            if(dataBase.tasks.containsKey(taskId)) {
                out.append("Задача #").append(taskId).append(" уже существует\n");
                return;
            }

            redirection = new ConsoleTaskNew(out, dataBase, taskId);
        }
        catch(NumberFormatException e) {
            out.append("Команда указана неверно\n");
        }
    }
    private void executeEdit(ArrayList<String> words, StringBuilder out) {
        try {
            if(words.size() == 1) {
                out.append("Необходимо сперва указать идентификатор\n");
                return;
            }

            var taskId = Integer.parseInt(words.get(1));
            if(!dataBase.tasks.containsKey(taskId)) {
                out.append("Задача #").append(taskId).append(" не существует\n");
                return;
            }

            redirection = new ConsoleTaskEdit(out, dataBase, taskId);
        }
        catch(NumberFormatException e) {
            out.append("Команда указана неверно\n");
        }
    }
    private void executeRemove(ArrayList<String> words, StringBuilder out) {
        try {
            if(words.size() == 1) {
                out.append("Необходимо сперва указать идентификатор\n");
                return;
            }

            var id = Integer.parseInt(words.get(1));
            out.append("Задача #").append(id);
            if(dataBase.removeTask(id))
                out.append(" успешно удалена\n");
            else
                out.append(" не была найдена\n");
        }
        catch(NumberFormatException e) {
            out.append("Команда указана неверно\n");
        }
    }

    private void executeComplete(ArrayList<String> words, StringBuilder out) {
        try {
            if(words.size() == 1) {
                out.append("Необходимо сперва указать идентификатор\n");
                return;
            }

            var id = Integer.parseInt(words.get(1));
            out.append("Задача #").append(id);
            if(dataBase.tasks.containsKey(id)) {
                var task = dataBase.tasks.get(id);
                if(task.status == TodoTask.Status.Done) {
                    out.append(" уже помечена как выполненная\n");
                }
                else {
                    task.status = TodoTask.Status.Done;
                    task.completionDate = null;
                    out.append(" была помечена как выполненная\n");
                }
            }
            else
                out.append(" не была найдена\n");
        }
        catch(NumberFormatException e) {
            out.append("Команда указана неверно\n");
        }
    }

    @Override
    public void execute(String line, StringBuilder out) {
        if(redirection != null) {
            redirection.execute(line, out);
            if(redirection.finished)
                redirection = null;
            return;
        }

        var words = parseCmdWords(line.trim());
        if(words.isEmpty())
            return;

        switch(words.get(0)) {
            case "help": executeHelp(words, out); break;
            case "list": executeList(words, out); break;
            case "new": executeNew(words, out); break;
            case "remove": executeRemove(words, out); break;
            case "edit": executeEdit(words, out); break;
            case "complete": executeComplete(words, out); break;
            default: out.append("Неизвестная команда\n"); break;
        }
    }
}
