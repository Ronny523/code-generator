package com.code.cli.pattern;

public class Client {

    public static void main(String[] args) {
        // 创建接收者
        Device tv = new Device("TV");
        Device stereo = new Device("Stereo");

        // 创建命令对象
        TurnOnCommand turnOnCommand = new TurnOnCommand(tv);
        TurnOffCommand turnOffCommand = new TurnOffCommand(stereo);

        // 创建调用者
        RemoteControl remote = new RemoteControl();

        // 执行命令
        remote.setCommand(turnOnCommand);
        remote.pressButton();

        remote.setCommand(turnOffCommand);
        remote.pressButton();
    }
}
