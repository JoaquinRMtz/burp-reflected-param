
package burp.userinterface;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.ITextEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Joaquin R. Martinez
 */
public class UInterface extends JPanel implements ActionListener {

    private DefaultTableModel requestTableModel, parametersTableModel;
    private JButton cleanRequestsButton;
    private ITextEditor msgeditorRequest, msgeditorResponse;
    private JCheckBox automaticSendCheck;
    private LinkedList<IHttpRequestResponse> requestsList;
    private LinkedList<LinkedList<IParameter>> parametersList;
    private LinkedList<IParameter> tempParamsList;
    private IBurpExtenderCallbacks ibec;
    private int contRequests;
    private JTable requestsTable, parametersTable;
    private IExtensionHelpers helpers;
    private int selectedRow;
    //private JTextField hostField;

    public UInterface(IBurpExtenderCallbacks ibec) {
        //super(new BorderLayout(10,10));
        this.setBackground(Color.WHITE);
        setLayout(new GridLayout());
        this.ibec = ibec;
        selectedRow = -1;
        this.helpers = ibec.getHelpers();
        this.requestsList = new LinkedList<>();
        this.parametersList = new LinkedList<>();
        contRequests = 1;
        automaticSendCheck = new JCheckBox("Add request to list (If sends CSRF Tokens)");
        this.cleanRequestsButton = new JButton("Clear requests table");
        this.cleanRequestsButton.addActionListener(this);
        this.requestTableModel = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        tempParamsList = null;
        this.parametersTableModel = new DefaultTableModel(new String[]{"name", "type"}, 0);
        
        //crear los httpMessageEditors para presentar los requests/responses de los usuarios 1 y 2 y el de CSRF
        this.msgeditorRequest = ibec.createTextEditor();
        msgeditorRequest.getComponent().add(new PopupMenu());
        this.msgeditorRequest.setEditable(false);
        this.msgeditorResponse = ibec.createTextEditor();
        this.msgeditorResponse.setEditable(false);
        //this.requestTableModel.
        //crear panel de requests
        JPanel pnlRequests = new JPanel();
        Border brdRequestList = new TitledBorder(new LineBorder(Color.BLACK), "Requests list");
        pnlRequests.setBorder(brdRequestList);
        BoxLayout bxl_proyecto = new BoxLayout(pnlRequests, BoxLayout.Y_AXIS);
        pnlRequests.setLayout(bxl_proyecto);
        //eleccion de proyecto
        //crear tabla requests

        requestsTable = new JTable();
        //tbl_requests.setEnabled(false);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = requestsTable.getSelectedRow();
                if (selectedRow != -1) {
                    IHttpRequestResponse http_msg = requestsList.get(selectedRow);
                    try {
                        tempParamsList = parametersList.get(selectedRow);
                        //LinkedList<ParameterWithMarkers> params = parametersList.get(selectedRow);
                        msgeditorRequest.setText(http_msg.getRequest());
                        msgeditorResponse.setText(http_msg.getResponse());
                        parametersTableModel.setRowCount(0);
                        for (IParameter get : tempParamsList) {
                            sendToParametersTable(get);
                        }
                    } catch (Exception ex) {
                        try {
                            ibec.getStderr().write(ex.getMessage().getBytes());
                        } catch (IOException ex1) { }
                    }
                }
            }
        });
        requestsTable.setModel(this.requestTableModel);
        JScrollPane sclTblRequests = new JScrollPane();
        sclTblRequests.setPreferredSize(new Dimension(500, 220));
        sclTblRequests.setViewportView(requestsTable);        
        pnlRequests.add(sclTblRequests);
        //crear panel preview HTTP
        //crear panel request preview
        JTabbedPane tabRequests = new JTabbedPane();
        //agregar al tab 2 los requestst/responeses del usuario 2
        tabRequests.add("Request", this.msgeditorRequest.getComponent());
        tabRequests.add("Response", this.msgeditorResponse.getComponent());
        //agregar al tab 2 los requestst/responeses del usuario 2
        //agragar los tabs del usuario 1 y 2 y el de CSRF al tab principal

        parametersTable = new JTable();
        parametersTable.setModel(this.parametersTableModel);
        parametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        parametersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = parametersTable.getSelectedRow();
                if (selected != -1 && selectedRow != -1) {
                    try {
                        IParameter parametro = tempParamsList.get(selected);
                        msgeditorRequest.setSearchExpression(parametro.getValue());
                        msgeditorResponse.setSearchExpression(parametro.getValue());
                    } catch (Exception ex) {
                        try {
                            ibec.getStderr().write(ex.getMessage().getBytes());
                        } catch (IOException ex1) {
                        }
                    }
                }
            }
        });
        JScrollPane sclTblTokens = new JScrollPane();
        sclTblTokens.setPreferredSize(new Dimension(400, 120));
        sclTblTokens.setViewportView(parametersTable);

        JPanel pnlbtnsTablaTokens = new JPanel();
        BoxLayout bxlPnlBtnTokens = new BoxLayout(pnlbtnsTablaTokens, BoxLayout.Y_AXIS);
        pnlbtnsTablaTokens.setLayout(bxlPnlBtnTokens);
        //jPanel.setPreferredSize(new Dimension(100,200));
        pnlbtnsTablaTokens.add(this.automaticSendCheck);

        JPanel pnlReflectedParams = new JPanel(new GridLayout());
        pnlReflectedParams.setBorder(new TitledBorder(
                new LineBorder(Color.BLACK), "Reflected parameters"));
        pnlReflectedParams.add(sclTblTokens);
               
        JPanel pnlClearRrequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlClearRrequests.add(this.cleanRequestsButton);
        pnlReflectedParams.add(pnlClearRrequests);
        
        JSplitPane splpnIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splpnIzquierdo.add(pnlRequests);
        splpnIzquierdo.add(pnlReflectedParams);              
        
        JPanel pnlIzquierdo = new JPanel(new BorderLayout());
        
        pnlIzquierdo.add(splpnIzquierdo, "Center");
        pnlIzquierdo.add(pnlClearRrequests, "South");
        
        JSplitPane contenedorPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contenedorPrincipal.add(pnlIzquierdo);
        contenedorPrincipal.add(tabRequests);
        
        contenedorPrincipal.setAutoscrolls(true);
        add(contenedorPrincipal);
        ibec.customizeUiComponent(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.requestsList.clear();
        this.parametersList.clear();
        //this.tempParamsList.clear();
        this.requestTableModel.setRowCount(0);
        this.parametersTableModel.setRowCount(0);
    }

    public void sendToRequestsTable(IHttpRequestResponse rq, LinkedList<IParameter> pwm) {
        this.requestsList.add(rq);
        this.parametersList.add(pwm);
        IRequestInfo requestInfo = this.ibec.getHelpers().analyzeRequest(rq);
        //sendToParametersTable(pwm);
        this.requestTableModel.addRow(new String[]{"" + contRequests++, requestInfo.getMethod(), requestInfo.getUrl().toString()});
    }

    private void sendToParametersTable(IParameter token) {
        //IParameter token = pwm.getParameter();
        String name = token.getName();
        String type = "";
        switch (token.getType()) {
            case IParameter.PARAM_COOKIE:
                type = "COOKIE";
                break;
            case IParameter.PARAM_BODY:
                type = "BODY";
                break;
            case IParameter.PARAM_URL:
                type = "URL";
                break;
            default:
                type = "NOT SUPPORTED YET";
                break;
        }
        this.parametersTableModel.addRow(new String[]{name, type});
    }

    public boolean automaticAdd() {
        return this.automaticSendCheck.isSelected();
    }

    /**
     * Busca una cadena en bytes y devuelve un par (comienzo y fin de la
     * cadena). Si no encuentra nada retorna NULL
     */
    /*private int[] indexOf(byte[] data, byte[] search, int start, int end) {
        int startIndex = helpers.indexOf(data, search, true, start, end);
        if (startIndex != -1) {
            int end_ = startIndex + search.length;
            return new int[]{startIndex, end_};
        }
        return null;
    }*/
    /**
     * Devuelve todas las coincidencias 'search' en 'data'.
     */
    /*
    private LinkedList<int[]> recursiveIndexOf(byte[] data, byte[] search) {
        LinkedList<int[]> ret = new LinkedList<>();
        int start = 0, end = data.length - 1;
        int[] index = null;
        index = indexOf(data, search, start, end);
        while (index != null) {
            ret.add(index);
            start = index[1]; //ahora empezara buscando desde donde termino el anterior
            index = indexOf(data, search, start, end);
        }
        return ret;
    }
    */
}
