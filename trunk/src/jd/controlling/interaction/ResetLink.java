package jd.controlling.interaction;

import java.io.Serializable;

import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.controlling.JDController;
import jd.event.ControlEvent;
import jd.plugins.DownloadLink;
import jd.utils.JDLocale;
import jd.utils.JDUtilities;


public class ResetLink extends Interaction implements Serializable {


    /**
     * 
     */
    private static final long serialVersionUID = -9071890385750062424L;
    /**
     * serialVersionUID
     */
    private static final String NAME              = JDLocale.L("interaction.resetLink.name","Downloadlink zurücksetzen");
    private static final String PARAM_LAST_OR_ALL = "LAST_OR_ALL";
    private static final Object[] OPTIONS = new Object[]{JDLocale.L("interaction.resetLink.options.all","all Links"),JDLocale.L("interaction.resetLink.options.lastLink","only last Link")};
    /**
     * Führt die Normale Interaction zurück. Nach dem Aufruf dieser methode
     * läuft der Download wie geowhnt weiter.
     */
    public static String        PROPERTY_QUESTION = "INTERACTION_" + NAME + "_QUESTION";
    public ResetLink() {
     }
    @Override
    

    public boolean doInteraction(Object arg) {
        logger.info("Starting Rest Link");
        String type= this.getStringProperty(PARAM_LAST_OR_ALL, (String)OPTIONS[1]);
        JDController controller = JDUtilities.getController();
        if(type.equals((String)OPTIONS[0])){
            controller.resetAllLinks();
        }else if(type.equals((String)OPTIONS[1])){
            DownloadLink link=controller.getLastFinishedDownloadLink();
            if(link!=null){
           link.setStatus(DownloadLink.STATUS_TODO);
           link.setStatusText("");
           link.reset();
           fireControlEvent(new ControlEvent(this, ControlEvent.CONTROL_SINGLE_DOWNLOAD_CHANGED, link));
            }else{
                logger.severe("Kein letzter Downloadlink gefunden");
            }
           
        }
       
      return true;
    }
    /**
     * Nichts zu tun. WebUpdate ist ein Beispiel für eine ThreadInteraction
     */
    public void run() {}
    public String toString() {
        return NAME;
    }
    @Override
    public String getInteractionName() {
        return NAME;
    }
    @Override
    public void initConfig() {
        //int type, Property propertyInstance, String propertyName, Object[] list, String label
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_COMBOBOX, this, PARAM_LAST_OR_ALL, OPTIONS,JDLocale.L("interaction.resetLink.whichLink","Welcher Link soll zurückgesetzt werden?")).setDefaultValue(OPTIONS[1]));
        
        
    }
    @Override
    public void resetInteraction() {}
}
