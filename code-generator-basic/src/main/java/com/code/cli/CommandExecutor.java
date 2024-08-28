package com.code.cli;

import com.code.cli.command.ConfigCommand;
import com.code.cli.command.GenerateCommand;
import com.code.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "code", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine comandLine;

    {
        // 这段初始化代码没有必要每次都执行（每次都是一样的），就可以用代码块，提前初始化好，节省资源开销（类的方法是静态时，就用静态代码块）
        comandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        // 不输出子命令时，给出提示（因为此父命令的作用，只是用来绑定子命令的）
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        return comandLine.execute(args);
    }

}
