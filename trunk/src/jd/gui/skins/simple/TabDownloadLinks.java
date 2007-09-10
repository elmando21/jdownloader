package jd.gui.skins.simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import jd.JDUtilities;
import jd.event.ControlEvent;
import jd.event.ControlListener;
import jd.plugins.DownloadLink;
import jd.plugins.Plugin;
import jd.plugins.event.PluginEvent;
import jd.plugins.event.PluginListener;
/**
 * Diese Tabelle zeigt alle zur Verfügung stehenden Downloads an.
 *
 * @author astaldo
 */
public class TabDownloadLinks extends JPanel implements PluginListener, ControlListener{
    private final int COL_INDEX    = 0;
    private final int COL_NAME     = 1;
    private final int COL_HOST     = 2;
    private final int COL_STATUS   = 3;
    private final int COL_PROGRESS = 4;

    private final Color COLOR_DONE     = new Color(  0,255,  0, 20);
    private final Color COLOR_ERROR    = new Color(255,  0,  0, 20);
    private final Color COLOR_DISABLED = new Color(100,100,100, 20);
    private final Color COLOR_WAIT= new Color(0,0,100, 20);
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3033753799006526304L;
    /**
     * Diese Tabelle enthält die eigentlichen DownloadLinks
     */
    private InternalTable             table;
    /**
     * Das interen TableModel, um die Daten anzuzeigen
     */
    private InternalTableModel internalTableModel = new InternalTableModel();
    /**
     * Dieser Vector enthält alle Downloadlinks
     */
    private Vector<DownloadLink> allLinks = new Vector<DownloadLink>();
    /**
     * Der Logger für Meldungen
     */
    private Logger logger = Plugin.getLogger();
    /**
     * Erstellt eine neue Tabelle
     *
     * @param parent Das aufrufende Hauptfenster
     */
    public TabDownloadLinks(SimpleGUI parent){
        super(new BorderLayout());
        table = new InternalTable();
        table.setModel(internalTableModel);
//        table.getColumn(table.getColumnName(COL_PROGRESS)).setCellRenderer(int);

        TableColumn column = null;
        for (int c = 0; c < internalTableModel.getColumnCount(); c++) {
            column = table.getColumnModel().getColumn(c);
            switch(c){
                case COL_INDEX:    column.setPreferredWidth(30);  break;
                case COL_NAME:     column.setPreferredWidth(200); break;
                case COL_HOST:     column.setPreferredWidth(150); break;
                case COL_STATUS: column.setPreferredWidth(200); break;
                case COL_PROGRESS: column.setPreferredWidth(250); break;
                
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800,450));
//        table.setPreferredSize(new Dimension(800,450));
        add(scrollPane);
    }
    public void setDownloadLinks(DownloadLink links[]){
        allLinks.clear();
        addLinks(links);
    }
    /**
     * Hier werden Links zu dieser Tabelle hinzugefügt.
     *
     * @param links Ein Vector mit Downloadlinks, die alle hinzugefügt werden sollen
     */
    public void addLinks(DownloadLink links[]){
        for(int i=0;i<links.length;i++){
            if(allLinks.indexOf(links[i])==-1)
                allLinks.add(links[i]);
            else
                logger.info("download-URL already in Queue");
        }
        fireTableChanged();
    }
    /**
     * Entfernt die aktuell selektierten Links
     */
    public void removeSelectedLinks(){
        Vector<DownloadLink> linksToDelete = getSelectedObjects();
        allLinks.removeAll(linksToDelete);
        table.getSelectionModel().clearSelection();
        fireTableChanged();
    }
    /**
     * Liefert alle selektierten Links zurück
     * @return Die selektierten Links
     */
    public Vector<DownloadLink> getSelectedObjects(){
        int rows[] = table.getSelectedRows();
        Vector<DownloadLink> linksSelected = new Vector<DownloadLink>();
        for(int i=0;i<rows.length;i++){
            linksSelected.add(allLinks.get(rows[i]));
        }
        return linksSelected;
    }
    public void setSelectedDownloadLinks(Vector<DownloadLink> selectedDownloadLinks){
        int index;
        Iterator<DownloadLink> iterator = selectedDownloadLinks.iterator();
        while(iterator.hasNext()){
            index = allLinks.indexOf(iterator.next());
            table.getSelectionModel().addSelectionInterval(index, index);
        }
    }
    /**
     * TODO Verschieben von zellen
     *
     * Hiermit werden die selektierten Zeilen innerhalb der Tabelle verschoben
     *
     * @param direction Zeigt wie/wohin die Einträge verschoben werden sollen
     */
    public void moveItems(int direction){
    }
    public Vector<DownloadLink> getLinks(){
        return allLinks;
    }
    /**
     * Hiermit wird die Tabelle aktualisiert
     * Die Markierte reihe wird nach dem ändern wieder neu gesetzt
     */
    public void fireTableChanged(){
        Vector<DownloadLink> selectedDownloadLinks = getSelectedObjects();
        table.tableChanged(new TableModelEvent(table.getModel()));
        setSelectedDownloadLinks(selectedDownloadLinks);
    }
    public void pluginEvent(PluginEvent event) {
        switch(event.getID()){
            case PluginEvent.PLUGIN_DATA_CHANGED:
                fireTableChanged();
                break;
        }
    }
    
    public void controlEvent(ControlEvent event) {
        switch(event.getID()){
            case ControlEvent.CONTROL_SINGLE_DOWNLOAD_CHANGED:
                fireTableChanged();
                break;
        }
        
    }
    private class InternalTable extends JTable{
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 4424930948374806098L;
        private InternalTableCellRenderer internalTableCellRenderer = new InternalTableCellRenderer();


        @Override
        public TableCellRenderer getCellRenderer(int arg0, int arg1) {
            return internalTableCellRenderer;
        }
    }
    /**
     * Dieses TableModel sorgt dafür, daß die Daten der Downloadlinks korrekt dargestellt werden
     *
     * @author astaldo
     */
    private class InternalTableModel extends AbstractTableModel{
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = -357970066822953957L;
        private String labelIndex    = JDUtilities.getResourceString("label.tab.download.column_index");
        private String labelLink     = JDUtilities.getResourceString("label.tab.download.column_link");
        private String labelHost     = JDUtilities.getResourceString("label.tab.download.column_host");
        private String labelStatus    = JDUtilities.getResourceString("label.tab.download.column_status");
        private String labelProgress = JDUtilities.getResourceString("label.tab.download.column_progress");
        @Override
        public String getColumnName(int column) {
            switch(column){
                case COL_INDEX:    return labelIndex;
                case COL_NAME:     return labelLink;
                case COL_STATUS :    return labelStatus;
                case COL_HOST :    return labelHost;
                case COL_PROGRESS: return labelProgress;
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch(columnIndex){
                case COL_INDEX:
//                    return Integer.class;
                case COL_NAME:
                case COL_STATUS:
                    return String.class;
                case COL_HOST:
                    return String.class;
                case COL_PROGRESS:
                    return JComponent.class;
            }
            return String.class;
        }
        public int getColumnCount() {
            return 5;
        }
        public int getRowCount() {
            return allLinks.size();
        }
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex< allLinks.size()){
                DownloadLink downloadLink = allLinks.get(rowIndex);
                switch(columnIndex){
                    case COL_INDEX:    return rowIndex;
                    case COL_NAME:     return downloadLink.getName();
                    case COL_STATUS:     return downloadLink.getStatusText();
                    case COL_HOST:     return downloadLink.getHost();
                    case COL_PROGRESS:
                        if (downloadLink.isInProgress()&&downloadLink.getRemainingWaittime()==0){
                            JProgressBar p = new JProgressBar(0,downloadLink.getDownloadMax());
                            p.setStringPainted(true);
                            p.setValue(downloadLink.getDownloadCurrent());
                            return p;
                        }else if(downloadLink.getRemainingWaittime()>0){
                            JProgressBar p = new JProgressBar(0,downloadLink.getWaitTime());
                            p.setBackground(new Color(255,  0,  0, 80));
                            p.setStringPainted(true);
                            
                            p.setValue((int)downloadLink.getRemainingWaittime());
                            return p;
                            
                        }
                        else
                            return null;
                }
            }
            return null;
        }
        public DownloadLink getDownloadLinkAtRow(int row) {
            return allLinks.get(row);
        }

    }
    private class InternalTableCellRenderer extends DefaultTableCellRenderer{
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = -3912572910439565199L;
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
            if(value instanceof JProgressBar)
                return (JProgressBar)value;

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(!isSelected){
                DownloadLink dLink = allLinks.get(row);
                if (!dLink.isEnabled()){
                    c.setBackground(COLOR_DISABLED);
                }
                else if(dLink.getRemainingWaittime()>0){
                    c.setBackground(COLOR_WAIT);
                }
                else if(dLink.getStatus()==DownloadLink.STATUS_DONE){
                    c.setBackground(COLOR_DONE);
                }
                
                
                
                else if(dLink.getStatus()!=DownloadLink.STATUS_TODO&&dLink.getStatus()!=DownloadLink.STATUS_ERROR_DOWNLOAD_LIMIT){
                    c.setBackground(COLOR_ERROR);
                }
                else
                    c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
