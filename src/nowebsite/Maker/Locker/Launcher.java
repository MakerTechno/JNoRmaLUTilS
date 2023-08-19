package nowebsite.Maker.Locker;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {
    public static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    /**Try to stop others twice.
     * If not it means the app is still running.
     * (GUI will check the exist file every 200 seconds，when it's not exist GUI will create one.)
     */
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
                LOGGER.log(Level.SEVERE, "Launcher was thrown an sleeping exception on Thread.", e);
            }
            return true;
        } else {
            SwingUtilities.invokeLater(() -> {
                GUI gui = new GUI(LOGGER);
                gui.setVisible(true);
            });
            return false;
        }
    }
}
