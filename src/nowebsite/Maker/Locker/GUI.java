package nowebsite.Maker.Locker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**加密解密软件のGUI
 * @author MakerTechno
 * 这个类使用中文注解哦~
 */
public class GUI extends JFrame {
    public final Logger LOGGER;

    /**这是加密按钮*/
    private final JButton ENCRYPT_BUTTON = new JButton("加密");
    /**这是解密按钮*/
    private final JButton DECRYPT_BUTTON = new JButton("解密");
    /**这是一个询问提示框*/
    private AskForReWrite dialog ;

    /**这tm是之前试了半天的系统临时文件夹获取方法*/
    public static final String TEMP_PATH = System.getenv("Temp");
    /**这是检查用的文件的位置*/
    public static final File FILE = new File(TEMP_PATH+File.separator+"tmp_isUsing.tmp");

    /**GUI存活与否*/
    private boolean isCycling = true;


    /**构造函数负责初始化整个GUI图形界面*/
    public GUI(Logger logger) {
        /*先头初始化*/
        this.generalSetUp(this, FILE);
        /*初始化Logger*/
        LOGGER = logger;
        /*主体部分*/
        JPanel mainPanel = getNewBorderPanel();

        /*文件选择部分*/
        JPanel filePanel = getNewBorderPanel();
        JTextField fileTextField = new JTextField();
        this.fileSelectorInit(fileTextField, filePanel);

        /*密码输入部分*/
        JPanel passwordPanel = getNewBorderPanel();
        JPasswordField userPasswordField = new JPasswordField();
        this.passwordAreaInit(userPasswordField, passwordPanel);

        /*加密解密模式选择部分*/
        JPanel buttonPanel = new JPanel();
        this.modeButtonInit(fileTextField, userPasswordField, this.ENCRYPT_BUTTON, this.DECRYPT_BUTTON, buttonPanel);

        /*在主体JPanel中添加上述模块*/
        mainPanel.add(filePanel, BorderLayout.NORTH);
        mainPanel.add(passwordPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        /*将主体JPanel添加至JFrame*/
        this.add(mainPanel);
    }

    /**增加代码复用性添加の创建器*/
    @Contract(" -> new")
    private @NotNull JPanel getNewBorderPanel(){
        return new JPanel(new BorderLayout());
    }

    /**最基础的属性显示设置*/
    @SuppressWarnings("all")
    private void generalSetUp(@NotNull JFrame frame, File tempFile){
        /*管理运行时の独立线程*/
        new Thread(() ->{
            while (isCycling){
                while (!FILE.exists()){
                    /*尝试创建Temp文件*/
                    try {
                        createAccessTemp(tempFile);
                        Thread.sleep(200);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        /*基础窗口设置*/
        frame.setTitle("文件加密/解密器(AES)");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        /*关闭时设置*/
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //记得删Temp啊喂
                isCycling = false;
                deleteAccessTemp(tempFile);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                super.windowClosing(e);
            }
        });
    }

    /**供给文件选择器の初始化操作*/
    private void fileSelectorInit(@NotNull JTextField field, @NotNull JPanel panel){
        /*文本域初始化设定*/
        field.setEditable(false);

        /*选择按钮初始化设定*/
        JButton fileButton = new JButton("选择文件");
        fileButton.addActionListener(e -> selectFile(this, field, "jEAl_const"));

        /*添加上述模块*/
        panel.add(field, BorderLayout.CENTER);
        panel.add(fileButton, BorderLayout.EAST);
    }

    /**供给密码域の初始化操作*/
    private void passwordAreaInit(@NotNull JPasswordField field, @NotNull JPanel panel){
        /*密码区前部说明文字设置*/
        JLabel textLabel = new JLabel("密码:");

        /*密码区明文与不可见切换按钮设置*/
        JButton button = new JButton("显示");
        button.addActionListener(e -> {
            if(e.getActionCommand().equals("显示")){
                field.setEchoChar('\0');//使密码栏明文可见
                button.setText("隐藏");
            } else {
                field.setEchoChar('*');//使密码栏重新不可见
                button.setText("显示");
            }
        });

        /*添加上述模块*/
        panel.add(textLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
    }

    /**供给模式选择部分の初始化操作*/
    private void modeButtonInit(@NotNull JTextField textField, @NotNull JPasswordField passwordField, @NotNull JButton encryptButton, @NotNull JButton decryptButton, @NotNull JPanel panel){
        /*加密按钮初始化*/
        encryptButton.addActionListener(e -> encryptFile(textField, passwordField));

        /*解密按钮初始化*/
        decryptButton.addActionListener(e -> decryptFile(textField, passwordField));

        /*添加上述模块*/
        panel.add(ENCRYPT_BUTTON);
        panel.add(DECRYPT_BUTTON);
    }

    /**作为每刻仅允许一个窗口启动のTemp*/
    @SuppressWarnings("all")
    public static void createAccessTemp(@NotNull File file) throws IOException {
        if (file.exists())return;
        file.createNewFile();
    }

    /**退出时删除Temp*/
    @SuppressWarnings("all")
    public static void deleteAccessTemp(@NotNull File file){
        file.delete();
    }

    /**供给提示弹窗の重写+初始化操作,唯一一个方法直接引用主类变量的地方*/
    private void dialogInit(String s){
        dialog = new AskForReWrite(this, true, s, LOGGER);
    }

    /**一个文件选择器*/
    private void selectFile(JFrame frame,@NotNull JTextField field, String ...extensions) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("文件类型", extensions);
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            field.setText(selectedFile.getAbsolutePath());
        }
    }

    /**加密文件の方法*/
    private void encryptFile(@NotNull JTextField textField, @NotNull JPasswordField passwordField) {
        /*阻塞按钮防止对最终信息修改*/
        trace();

        /*获取输入文件路径和密码*/
        String inputFile = textField.getText();
        String password = new String(passwordField.getPassword());

        /*要求输入加密位置*/
        File encryptedFile = explore("加密文件.jEAl_const");
        if (encryptedFile == null){
            unTrace();
            return;
        }
        /*执行加密*/

        try {
            AESLogicLib.KeyFormer former = new AESLogicLib.KeyFormer(password, LOGGER);
            AESLogicLib.encryptFileWithName(new File(inputFile), encryptedFile,  former, LOGGER);
        } catch (SaltInputException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Exception was thrown during encrypt file:", e);
        }

        /*任务结束，停止阻塞*/
        unTrace();
    }

    /**解密文件の方法*/
    @SuppressWarnings("all")
    private void decryptFile(@NotNull JTextField textField, @NotNull JPasswordField passwordField) {
        /*阻塞按钮防止对最终信息修改*/
        trace();

        /*获取输入文件路径和密码*/
        String inputFile = textField.getText();
        String password = new String(passwordField.getPassword());

        /*预处理解密后文件位置并记为缓存*/
        File decryptedFile = explore("缓存"+(new Random().nextInt(65536)+65536) +".114514"); // 获取导出位置
        if (decryptedFile == null){
            unTrace();
            return;
        }


        /*执行解密*/
        try {
            AESLogicLib.KeyFormer former = new AESLogicLib.KeyFormer(password, LOGGER);
            String newName = AESLogicLib.decryptFileWithName(new File(inputFile), decryptedFile, former, LOGGER);

            /*抓取解密错误*/
            if(Objects.equals(newName, "error")){
                JOptionPane.showMessageDialog(null, "也许是秘钥错误，请检查秘钥是否正确！", "发生严重错误", JOptionPane.ERROR_MESSAGE);
                decryptedFile.delete();
                unTrace();
                return;
            }

            /*重命名解密文件,如果冲突则询问*/
            File newFile = new File(decryptedFile.getParentFile().getAbsolutePath() + File.separator + newName);
            decryptedFile.renameTo(askForSameNameFileRename(decryptedFile, newFile));

        } catch (SaltInputException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        //任务结束，停止阻塞
        unTrace();
    }

    /**检查并对文件做一些基础设置*/
    @SuppressWarnings("all")
    private @NotNull File askForSameNameFileRename(File redyFile, @NotNull File file){
        Random random = new Random();
        /*如果文件存在，弹窗要求回应，并通过回应决定返回旧文件还是新文件*/
        if (file.exists()) {
            /*调用弹窗初始化和显示*/
            dialogInit(file.getName());
            /*循环检查用户输入是否完成，约等于阻塞到用户返回*/
            while (!dialog.knockBack) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //此时返回已完成
            if (dialog.answer) {
                file.delete();
                return file;
            } else {
                return new File(redyFile.getParentFile().getAbsolutePath() + File.separator + (random.nextInt(65536) + 65536) + file.getName());
            }
        } else {
            return file;
        }
    }

    /**对窗口内容阻塞，禁止其它操作*/
    private void trace(){
        //阻塞按钮
        DECRYPT_BUTTON.setText("请稍后...");
        DECRYPT_BUTTON.setEnabled(false);
        ENCRYPT_BUTTON.setText("请稍后...");
        ENCRYPT_BUTTON.setEnabled(false);
        this.setEnabled(false);
    }

    /**取消对窗口的阻塞*/
    private void unTrace(){
        //取消阻塞
        DECRYPT_BUTTON.setText("解密");
        DECRYPT_BUTTON.setEnabled(true);
        ENCRYPT_BUTTON.setText("加密");
        ENCRYPT_BUTTON.setEnabled(true);
        this.setEnabled(true);
        //刷新一下，因为之前有什么地方感觉好奇怪
        this.setVisible(true);
    }

    public File explore(String fileName) {
        /*选择导出位置*/
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择导出位置");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String exportPath = exportPathChooser(fileChooser);
        if (exportPath != null) {
            String exportFilePath = exportPath + fileName;

            /*导出解密后的文件到选择的位置,因为只有一个文件重写问题，所以两个都填同一个就行*/
            File exportFile = new File(exportFilePath);
            return askForSameNameFileRename(exportFile, exportFile);
        } else {
            return null;
        }
    }

    /**召唤保存选择器*/
    private @Nullable String exportPathChooser(@NotNull JFileChooser fileChooser) {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            return selectedDirectory.getAbsolutePath() + File.separator;
        } else return null;
    }
/*
    //测试模块
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
*/
}

/**一个修改自JDialog的类，添加了初始化操作与一些成员变量以匹配GUI需求*/
class AskForReWrite extends JDialog{
    public final Logger LOGGER;
    public boolean answer = false;
    public boolean knockBack = false;

    public AskForReWrite(Frame parent, boolean isTrace, String fileName, Logger logger){
        super(parent, isTrace);
        LOGGER = logger;
        /*基础初始化*/
        generalSetup();

        /*主面板*/
        JPanel mainPanel = new JPanel(new BorderLayout());

        /*文字提示部位*/
        JTextArea textAreaD = new JTextArea("文件"+fileName+"已存在，是否替换？");
        textAreaD.setEditable(false);
        mainPanel.add(textAreaD, BorderLayout.NORTH);

        /*按钮选择部位*/
        JPanel buttonPanel = new JPanel();
        buttonPartInit(buttonPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        /*添加上述模块*/
        this.add(mainPanel);
        /*可视化*/
        this.setVisible(true);
    }

    /**基础の设置*/
    private void generalSetup(){
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(300, 100);
        this.setLocationRelativeTo(null);
        finalizationSetup();
    }

    /**回应按钮の设置*/
    private void buttonPartInit(@NotNull JPanel panel){
        /*同意按钮*/
        JButton acceptButton = new JButton("是");
        acceptButton.addActionListener(e -> {
            this.answer = true;
            this.finalizeTip();
        });

        /*不同意按钮*/
        JButton refuseButton = new JButton("否");
        refuseButton.addActionListener(e -> {
            this.answer = false;
            this.finalizeTip();
        });

        /*添加上述模块*/
        panel.add(acceptButton);
        panel.add(refuseButton);
    }

    /**对于窗口关闭动作截获の初始化*/
    private void finalizationSetup(){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setKnockBack(true);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                   LOGGER.log(Level.SEVERE, "JDialog was thrown an sleeping exception on Thread.", ex);
                }
                super.windowClosing(e);
            }
        });
    }

    /**对于最终销毁的设置*/
    public void finalizeTip(){
        this.knockBack = true;
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**外接一个设置*/
    public void setKnockBack(boolean knockBack) {
        this.knockBack = knockBack;
    }
}

