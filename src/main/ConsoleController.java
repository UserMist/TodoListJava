package main;

/**
 * Консольный контроллер. При вызове "execute" он выполняет некие действия и записывает в аргумент "out" текстовый ответ для передачи на консоль.
 * По завершению работы устанавливает флаг needsDispose = true, после чего "execute" запрещено снова вызывать.
 */
public abstract class ConsoleController {
    public boolean needsDispose = false;
    public abstract void execute(String line, StringBuilder out);
}
