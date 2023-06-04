package nowebsite.Maker.Locker;

import javax.swing.*;

public class Launcher {

    /**尝试删两次，不成功说明程序还在运行(GUI会每200毫秒检查文件存在，不存在时会再新建一个)*/
    public static void main(String[] args) {
        if (startGUI())if (startGUI())System.out.println("检测到当前系统有本程序运行，请先关闭程序再重新运行");
    }

    /**如果文件存在，尝试删一下，毕竟有可能是之前宕机没删。不存在，那就直接上*/
    private static boolean startGUI(){
        if (GUI.FILE.exists()){
            GUI.deleteAccessTemp(GUI.FILE);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            SwingUtilities.invokeLater(() -> {
                GUI gui = new GUI();
                gui.setVisible(true);
            });
            return false;
        }
    }
}
