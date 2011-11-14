//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.swing.jdgui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import jd.config.ConfigPropertyListener;
import jd.config.Configuration;
import jd.config.Property;
import jd.controlling.ClipboardHandler;
import jd.controlling.IOEQ;
import jd.controlling.JDController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.reconnect.Reconnecter;
import jd.event.ControlEvent;
import jd.event.ControlIDListener;
import jd.gui.UserIF;
import jd.gui.UserIO;
import jd.gui.swing.SwingGui;
import jd.gui.swing.jdgui.components.premiumbar.PremiumStatus;
import jd.gui.swing.jdgui.views.settings.panels.addons.ExtensionManager;
import jd.gui.swing.jdgui.views.settings.panels.passwords.PasswordList;
import jd.nutils.JDFlags;
import jd.utils.JDUtilities;
import jd.utils.WebUpdate;

import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.settings.GeneralSettings;

/**
 * Class to control toolbar actions
 * 
 * @author Coalado
 */
public class ActionController {
    public static final String              JDL_PREFIX          = "jd.gui.swing.jdgui.actions.ActionController.";
    private static ArrayList<ToolBarAction> TOOLBAR_ACTION_LIST = new ArrayList<ToolBarAction>();
    private static boolean                  initiated           = false;

    /**
     * returns a fresh copy of all toolbaractions
     * 
     * @return
     */
    public static ArrayList<ToolBarAction> getActions() {
        initActions();
        final ArrayList<ToolBarAction> ret = new ArrayList<ToolBarAction>();
        synchronized (ActionController.TOOLBAR_ACTION_LIST) {
            ret.addAll(ActionController.TOOLBAR_ACTION_LIST);
        }
        return ret;

    }

    /**
     * Returns the action for the givven key
     * 
     * @param keyid
     * @return
     */
    public static ToolBarAction getToolBarAction(final String keyid) {
        initActions();
        synchronized (ActionController.TOOLBAR_ACTION_LIST) {
            for (final ToolBarAction a : ActionController.TOOLBAR_ACTION_LIST) {
                if (a.getID().equals(keyid)) { return a; }
            }
            return null;
        }
    }

    /**
     * Defines all possible actions
     */
    private static void initActions() {
        if (initiated) return;
        synchronized (ActionController.TOOLBAR_ACTION_LIST) {
            if (initiated) return;
            initiated = true;
            new ToolBarAction(_GUI._.action_seperator(), "toolbar.separator", null) {
                private static final long serialVersionUID = -4628452328096482738L;

                @Override
                public void initDefaults() {
                    this.setType(ToolBarAction.Types.SEPARATOR);
                }

                @Override
                public void onAction(final ActionEvent e) {
                }

                @Override
                protected String createMnemonic() {
                    return null;
                }

                @Override
                protected String createAccelerator() {
                    return null;
                }

                @Override
                protected String createTooltip() {
                    return null;
                }

            };

            new ToolBarAction(_GUI._.action_start_downloads(), "toolbar.control.start", "media-playback-start") {
                private static final long serialVersionUID = 1683169623090750199L;

                @Override
                public void initAction() {
                    JDController.getInstance().addControlListener(new ControlIDListener(ControlEvent.CONTROL_DOWNLOAD_START, ControlEvent.CONTROL_DOWNLOAD_STOP) {
                        @Override
                        public void controlIDEvent(final ControlEvent event) {
                            switch (event.getEventID()) {
                            case ControlEvent.CONTROL_DOWNLOAD_START:
                                setEnabled(false);
                                break;
                            case ControlEvent.CONTROL_DOWNLOAD_STOP:
                                setEnabled(true);
                                break;
                            }
                        }
                    });
                }

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    DownloadWatchDog.getInstance().startDownloads();
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_start_downloads_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_start_downloads_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return null;
                }

            };
            new ToolBarAction(_GUI._.action_pause(), "toolbar.control.pause", "media-playback-pause") {
                private static final long serialVersionUID = 7153300370492212502L;

                @Override
                public void initAction() {
                    JDController.getInstance().addControlListener(new ControlIDListener(ControlEvent.CONTROL_DOWNLOAD_START, ControlEvent.CONTROL_DOWNLOAD_STOP) {
                        @Override
                        public void controlIDEvent(final ControlEvent event) {
                            switch (event.getEventID()) {
                            case ControlEvent.CONTROL_DOWNLOAD_START:
                                setEnabled(true);
                                setSelected(false);
                                break;
                            case ControlEvent.CONTROL_DOWNLOAD_STOP:
                                setEnabled(false);
                                setSelected(false);
                                break;
                            }
                        }
                    });

                    GeneralSettings.PAUSE_SPEED.getEventSender().addListener(new GenericConfigEventListener<Integer>() {

                        public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
                        }

                        public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
                            new EDTRunner() {

                                @Override
                                protected void runInEDT() {
                                    setToolTipText(_GUI._.gui_menu_action_break2_desc(GeneralSettings.PAUSE_SPEED.getValue()));

                                }
                            };
                        }

                    });

                }

                @Override
                public void initDefaults() {
                    this.setEnabled(false);
                    this.setType(ToolBarAction.Types.TOGGLE);
                    setToolTipText(_GUI._.gui_menu_action_break2_desc(JsonConfig.create(GeneralSettings.class).getPauseSpeed()));

                }

                @Override
                public void onAction(final ActionEvent e) {
                    final boolean b = ActionController.getToolBarAction("toolbar.control.pause").isSelected();
                    DownloadWatchDog.getInstance().pauseDownloadWatchDog(b);
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_pause_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_pause_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_pause_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_stop_downloads(), "toolbar.control.stop", "media-playback-stop") {
                private static final long serialVersionUID = 1409143759105090751L;

                @Override
                public void initAction() {
                    JDController.getInstance().addControlListener(new ControlIDListener(ControlEvent.CONTROL_DOWNLOAD_START, ControlEvent.CONTROL_DOWNLOAD_STOP) {
                        @Override
                        public void controlIDEvent(final ControlEvent event) {
                            switch (event.getEventID()) {
                            case ControlEvent.CONTROL_DOWNLOAD_START:
                                setEnabled(true);
                                break;
                            case ControlEvent.CONTROL_DOWNLOAD_STOP:
                                setEnabled(false);
                                break;
                            }
                        }
                    });
                }

                @Override
                public void initDefaults() {
                    this.setEnabled(false);
                }

                @Override
                public void onAction(final ActionEvent e) {
                    IOEQ.add(new Runnable() {

                        public void run() {
                            if (DownloadWatchDog.getInstance().getStateMonitor().hasPassed(DownloadWatchDog.STOPPING_STATE)) return;
                            DownloadWatchDog.getInstance().stopDownloads();
                        }

                    });

                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_stop_downloads_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_stop_downloads_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_stop_downloads_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_reconnect_invoke(), "toolbar.interaction.reconnect", "reconnect") {
                private static final long serialVersionUID = -1295253607970814759L;

                @Override
                public void initAction() {
                    // ]
                    // Reconnecter.getInstance().getEventSender().addListener(new
                    // DefaultEventListener<ReconnecterEvent>() {
                    // // TODO: test
                    // public void onEvent(final ReconnecterEvent event) {
                    // if (event.getEventID() ==
                    // ReconnecterEvent.SETTINGS_CHANGED)
                    // {
                    // final StorageValueChangeEvent<?> storageEvent =
                    // (StorageValueChangeEvent<?>) event.getParameter();
                    // if (storageEvent.getKey() ==
                    // Reconnecter.RECONNECT_FAILED_COUNTER) {
                    //
                    // if (((Number) storageEvent.getNewValue()).longValue() >
                    // 5) {
                    // setIcon("reconnect_warning");
                    // setToolTipText(_GUI._.gui_menu_action_reconnect_notconfigured_tooltip());
                    // ActionController.getToolBarAction("toolbar.quickconfig.reconnecttoggle").setToolTipText(_GUI._.gui_menu_action_reconnect_notconfigured_tooltip());
                    // } else {
                    // setToolTipText(_GUI._.gui_menu_action_reconnectman_desc());
                    // setIcon("reconnect");
                    // ActionController.getToolBarAction("toolbar.quickconfig.reconnecttoggle").setToolTipText(_GUI._.gui_menu_action_reconnectauto_desc());
                    // }
                    // }
                    //
                    // }
                    // }
                    //
                    // });

                }

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    if (JDFlags.hasSomeFlags(UserIO.getInstance().requestConfirmDialog(0, _GUI._.gui_reconnect_confirm()), UserIO.RETURN_OK, UserIO.RETURN_DONT_SHOW_AGAIN)) {
                        /* forceReconnect is running in its own thread */
                        new Thread(new Runnable() {
                            public void run() {
                                Reconnecter.getInstance().forceReconnect();
                            }
                        }).start();
                    }
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_reconnect_invoke_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_reconnect_invoke_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_reconnect_invoke_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_start_update(), "toolbar.interaction.update", "update") {
                private static final long serialVersionUID = 4359802245569811800L;

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    /* WebUpdate is running in its own Thread */
                    WebUpdate.doUpdateCheck(true);
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_start_update_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_start_update_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_start_update_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_clipboard_observer(), "toolbar.quickconfig.clipboardoberserver", "clipboard") {
                private static final long serialVersionUID = -6442494647304101403L;

                @Override
                public void initAction() {
                    JDController.getInstance().addControlListener(new ConfigPropertyListener(Configuration.PARAM_CLIPBOARD_ALWAYS_ACTIVE) {
                        @Override
                        public void onPropertyChanged(final Property source, final String key) {
                            setSelected(source.getBooleanProperty(key, true));
                        }
                    });
                }

                @Override
                public void initDefaults() {
                    this.setType(ToolBarAction.Types.TOGGLE);
                    this.setSelected(JDUtilities.getConfiguration().getBooleanProperty(Configuration.PARAM_CLIPBOARD_ALWAYS_ACTIVE, true));
                }

                @Override
                public void onAction(final ActionEvent e) {
                    ClipboardHandler.getClipboard().setEnabled(this.isSelected());
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_clipboard_observer_mnemonics();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_clipboard_observer_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_clipboard_observer_tooltip();
                }
            };

            new ToolBarAction(_GUI._.action_reconnect_toggle(), "toolbar.quickconfig.reconnecttoggle", "reconnect") {
                private static final long serialVersionUID = -2942320816429047941L;

                @Override
                public void initAction() {
                    JDController.getInstance().addControlListener(new ConfigPropertyListener(Configuration.PARAM_ALLOW_RECONNECT) {
                        @Override
                        public void onPropertyChanged(final Property source, final String key) {
                            setSelected(source.getBooleanProperty(key, true));
                        }
                    });
                }

                @Override
                public void initDefaults() {
                    this.setType(ToolBarAction.Types.TOGGLE);
                    this.setSelected(JDUtilities.getConfiguration().getBooleanProperty(Configuration.PARAM_ALLOW_RECONNECT, true));
                }

                @Override
                public void onAction(final ActionEvent e) {
                    Reconnecter.getInstance().setAutoReconnectEnabled(!Reconnecter.getInstance().isAutoReconnectEnabled());
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_reconnect_toggle_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_reconnect_toggle_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_reconnect_toggle_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_open_dlfolder(), "action.opendlfolder", "package_open") {
                private static final long serialVersionUID = -60944746807335951L;

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    final String dlDir = JsonConfig.create(GeneralSettings.class).getDefaultDownloadFolder();
                    if (dlDir == null) { return; }
                    JDUtilities.openExplorer(new File(dlDir));
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_open_dlfolder_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_open_dlfolder_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_open_dlfolder_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_stopsign(), "toolbar.control.stopmark", "event") {
                private static final long serialVersionUID = 4359802245569811800L;

                @Override
                protected void initAction() {
                    JDController.getInstance().addControlListener(new ControlIDListener(ControlEvent.CONTROL_DOWNLOAD_START, ControlEvent.CONTROL_DOWNLOAD_STOP) {
                        @Override
                        public void controlIDEvent(final ControlEvent event) {
                            switch (event.getEventID()) {
                            case ControlEvent.CONTROL_DOWNLOAD_START:
                                setEnabled(true);
                                break;
                            case ControlEvent.CONTROL_DOWNLOAD_STOP:
                                setEnabled(false);
                                break;
                            }
                        }
                    });
                }

                @Override
                public void initDefaults() {
                    this.setToolTipText(_GUI._.jd_gui_swing_jdgui_actions_ActionController_toolbar_control_stopmark_tooltip());
                    this.setEnabled(false);
                    this.setType(ToolBarAction.Types.TOGGLE);
                    this.setSelected(false);
                }

                @Override
                public void onAction(final ActionEvent e) {
                    IOEQ.add(new Runnable() {

                        public void run() {
                            if (DownloadWatchDog.getInstance().isStopMarkSet()) {
                                DownloadWatchDog.getInstance().setStopMark(null);
                            } else if (DownloadWatchDog.getInstance().getActiveDownloads() > 0) {
                                DownloadWatchDog.getInstance().setStopMark(DownloadWatchDog.STOPMARK.RANDOM);
                            } else {
                                setSelected(false);
                            }
                            /* TODO:TODO */
                            // if
                            // (DownloadWatchDog.getInstance().getDownloadStatus()
                            // !=
                            // DownloadWatchDog.STATE.RUNNING &&
                            // !DownloadWatchDog.getInstance().isStopMarkSet())
                            // {
                            // this.setEnabled(false);
                            // }
                        }
                    });
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_stopsign_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_stopsign_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_stopsign_tooltip();
                }

            };

            new ToolBarAction(_GUI._.action_settings(), "addonsMenu.configuration", "extension") {
                private static final long serialVersionUID = -3613887193435347389L;

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    SwingGui.getInstance().requestPanel(UserIF.Panels.CONFIGPANEL, ExtensionManager.class);
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_settings_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_settings_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_settings_tooltip();
                }
            };
            new ToolBarAction(_GUI._.action_premium_toggle(), "premiumMenu.toggle", "premium") {

                private static final long serialVersionUID = 4276436625882302179L;

                @Override
                public void initDefaults() {
                    this.setType(ToolBarAction.Types.TOGGLE);
                    this.setSelected(JDUtilities.getConfiguration().getBooleanProperty(Configuration.PARAM_USE_GLOBAL_PREMIUM, true));

                    JDController.getInstance().addControlListener(new ConfigPropertyListener(Configuration.PARAM_USE_GLOBAL_PREMIUM) {
                        @Override
                        public void onPropertyChanged(final Property source, final String key) {
                            final boolean b = source.getBooleanProperty(key, true);
                            setSelected(b);
                            PremiumStatus.getInstance().updateGUI(b);
                        }
                    });
                }

                @Override
                public void onAction(final ActionEvent e) {
                    if (!this.isSelected()) {
                        final int answer = UserIO.getInstance().requestConfirmDialog(UserIO.DONT_SHOW_AGAIN | UserIO.DONT_SHOW_AGAIN_IGNORES_CANCEL, _GUI._.dialogs_premiumstatus_global_title(), _GUI._.dialogs_premiumstatus_global_message(), UserIO.getInstance().getIcon(UserIO.ICON_WARNING), _GUI._.gui_btn_yes(), _GUI._.gui_btn_no());
                        if (JDFlags.hasAllFlags(answer, UserIO.RETURN_CANCEL)) {
                            this.setSelected(true);
                            return;
                        }
                    }
                    JDUtilities.getConfiguration().setProperty(Configuration.PARAM_USE_GLOBAL_PREMIUM, this.isSelected());
                    JDUtilities.getConfiguration().save();
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_premium_toggle_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_premium_toggle_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_premium_toggle_tooltip();
                }

            };
            new ToolBarAction(_GUI._.action_premium_manager(), "premiumMenu.configuration", "premium") {
                private static final long serialVersionUID = -3613887193435347389L;

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    SwingGui.getInstance().requestPanel(UserIF.Panels.PREMIUMCONFIG, null);
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_premium_manager_mnemonic();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_premium_manager_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_premium_manager_tooltip();
                }
            };

            new ToolBarAction(_GUI._.action_passwordlist(), "action.passwordlist", PasswordList.getIconKey()) {
                private static final long serialVersionUID = -4111402172655120550L;

                @Override
                public void initDefaults() {
                }

                @Override
                public void onAction(final ActionEvent e) {
                    SwingGui.getInstance().requestPanel(UserIF.Panels.CONFIGPANEL, PasswordList.class);
                }

                @Override
                protected String createMnemonic() {
                    return _GUI._.action_passwordlist_mnemonics();
                }

                @Override
                protected String createAccelerator() {
                    return _GUI._.action_passwordlist_accelerator();
                }

                @Override
                protected String createTooltip() {
                    return _GUI._.action_passwordlist_tooltip();
                }
            };
        }
    }

    public static void register(final ToolBarAction action) {
        synchronized (ActionController.TOOLBAR_ACTION_LIST) {
            if (ActionController.TOOLBAR_ACTION_LIST.contains(action)) { return; }
            for (final ToolBarAction act : ActionController.TOOLBAR_ACTION_LIST) {
                if (act.getID().equalsIgnoreCase(action.getID())) { return; }
            }
            ActionController.TOOLBAR_ACTION_LIST.add(action);
        }
    }

    public static void unRegister(final ToolBarAction action) {
        synchronized (ActionController.TOOLBAR_ACTION_LIST) {
            if (!ActionController.TOOLBAR_ACTION_LIST.contains(action)) { return; }

            ActionController.TOOLBAR_ACTION_LIST.remove(action);
        }
    }

}