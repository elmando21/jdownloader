package jd.gui.userio;

import java.io.File;

import javax.swing.ImageIcon;

import jd.gui.UserIO;
import jd.gui.skins.simple.GuiRunnable;
import jd.gui.userio.dialog.CaptchaDialog;
import jd.gui.userio.dialog.ConfirmDialog;
import jd.gui.userio.dialog.InputDialog;
import jd.utils.JDTheme;

public class SimpleUserIO extends UserIO {
    private SimpleUserIO() {
        super();
    }

    public static UserIO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimpleUserIO();
        }
        return INSTANCE;
    }

    // @Override
    protected String showCaptchaDialog(final int flag, final String methodname, final File captchafile, final String suggestion, final String explain) {
        if ((flag & UserIO.NO_USER_INTERACTION) > 0) return suggestion;
        return new GuiRunnable<String>() {

            // @Override
            public String runSave() {
                return new CaptchaDialog(flag, methodname, captchafile, suggestion, explain).getCaptchaText();
            }
        }.getReturnValue();

    }

    public static void main(String args[]) {
        UserIO.setInstance(SimpleUserIO.getInstance());
        // String res = UserIO.getInstance().requestCaptchaDialog(0,
        // "megaupload.com", new File(
        // "C:\\Users\\oem\\.jd_home\\captchas\\megaupload.com\\23.04.2009_12.28.22.245.jpg"
        // ), "01234", null);
        // System.out.println("result: " + res);

       UserIO.getInstance().requestMessageDialog("TEST");
    }

    // @Override
    protected int showConfirmDialog(final int flag, final String title, final String message, final ImageIcon icon, final String okOption, final String cancelOption) {
        if ((flag & UserIO.NO_USER_INTERACTION) > 0) return 0;
        return new GuiRunnable<Integer>() {

            // @Override
            public Integer runSave() {
                return new ConfirmDialog(flag, title, message, icon, okOption, cancelOption).getReturnID();
            }
        }.getReturnValue();
    }

    @Override
    protected String showInputDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) {
        if ((flag & UserIO.NO_USER_INTERACTION) > 0) return defaultMessage;
        return new GuiRunnable<String>() {

            // @Override
            public String runSave() {
                return new InputDialog(flag, title, message, defaultMessage, icon, okOption, cancelOption).getReturnID();
            }
        }.getReturnValue();
    }

    @Override
    public ImageIcon getIcon(int iconInfo) {
        switch (iconInfo) {
        case UserIO.ICON_ERROR:
            return JDTheme.II("gui.images.stop", 32, 32);
        case UserIO.ICON_WARNING:
            return JDTheme.II("gui.images.warning", 32, 32);
        default:
            return JDTheme.II("gui.images.config.tip", 32, 32);
        }

    }
}
