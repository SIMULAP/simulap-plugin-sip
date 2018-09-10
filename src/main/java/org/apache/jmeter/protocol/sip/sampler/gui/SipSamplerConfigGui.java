//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.sampler.gui;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;

import javax.sip.message.Request;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBException;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;

import org.apache.jmeter.protocol.sip.sampler.SipSampler;
import org.apache.jmeter.protocol.sip.utils.SipDico;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.hp.simulap.sip.headers.Header;

/*******************************************************************************
 * This class MmeSamplerConfigGui is user interface gui for getting all the
 * configuration value from the user
 ******************************************************************************/

public class SipSamplerConfigGui extends AbstractSamplerGui implements
		TreeSelectionListener, ActionListener, TableModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8252795145202180613L;

	private static final int AVP_DETAIL_ALL_NOT_VISIBLE = 0;
	private static final int AVP_DETAIL_ALL_VISIBLE = 1;
	private static final int AVP_DETAIL_SHOW_TEXT = 2;
	private static final int AVP_DETAIL_SHOW_ENUM = 3;
	enum DialogNb { Dial1, Dial2, Dial3, Dial4, Dial5, Dial6, Dial7, Dial8, Dial9, Dial10 };
	enum TransactionNb { trans1, trans2, trans3, trans4, trans5, trans6, trans7, trans8, trans9, trans10, trans11, trans12, trans13, trans14, trans15 };
    
	private DynamicVerticalPanel mainPanel = new DynamicVerticalPanel();

	private JTextField sipNodeName = new JTextField(20);
	private JComboBox sipCommandSelector = new JComboBox();
	private VerticalPanel sipCommandPanel = new VerticalPanel();
	private JComboBox sipDialogueSelector = new JComboBox();
	private JComboBox sipTransactionSelector = new JComboBox();
	private JComboBox sipRelatedTransactionSelector = new JComboBox();
	private JTextField responseCodeValue = new JTextField(10);
	private VerticalPanel responseCodePanel = new VerticalPanel();
	private VerticalPanel detailPanel = new VerticalPanel();

    private JLabel avpLabel = new JLabel("Sip Message headers");
    
    private JButton[] buttons = { new JButton("Remove"), 
    							  new JButton("Up"), new JButton("Down"), new JButton("Add") };
    
    private JRadioButton sendDirectionRadio = new JRadioButton("Send");
    private JRadioButton receiveDirectionRadio = new JRadioButton("Receive");
    private JRadioButton resetRadio = new JRadioButton("ResetCall");
    private JRadioButton requestRadio = new JRadioButton("Request");
    private JRadioButton responseRadio = new JRadioButton("Response");
	private boolean displayName = true;
	private boolean saveTreeValue = true;
	private JTextField sipRcvTimeout = new JTextField(20);
	private JRadioButton optionalRadio = new JRadioButton("Optional");
	private JRadioButton ignoreRetransmissionRadio = new JRadioButton("IgnoreRetransmission");
	private JLabel currentLabel = new JLabel();
	private JTextField currentValue = new JTextField(20);
	private JComboBox enumValueSelector = new JComboBox();
	private JTable table = null;
	private DefaultTableModel dataModel = null;
	private int i =0;
	private JComboBox sipHeaderSelector = new JComboBox();
	
	private List<Header> dico = null;
	
    private JLabel bodyLabel = new JLabel("Sip Body part");
	private JTextArea bodyText = new JTextArea(10, 20);
	
    private static final Logger _logger = LoggingManager.getLoggerForClass();

	/**
	 * new introduced elements
	 */
	

	/***************************************************************************
	 * Default constructor for SipSamplerConfigGui
	 **************************************************************************/
	public SipSamplerConfigGui() {
		
		_logger.debug("SipSamplerConfigGui");
		init();

	}
	


	/***************************************************************************
	 * This will initalize all the panel in the SipSamplerConfigGui
	 **************************************************************************/
	protected void init() {
		_logger.debug("init ");
		try {
			dico = SipDico.loadDico();	
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			_logger.info("load dico failed: File parsing issue ");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			_logger.info("load dico failed: File not found ");
		}

		setName("name2");
		setComment("Comment2");
		
		setLayout(new BorderLayout(0, 10));
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		mainPanel.setName("name1");
		createSipNodeNamePanel(mainPanel);
		createDirectionPanel(mainPanel);
		createCommandPanel(mainPanel);
		createControlPanel(mainPanel);
		createTreePanel(mainPanel);
		
		add(mainPanel);//, BorderLayout.CENTER);
	}

    public String getStaticLabel() {
        return "SipCommandSampler";
    }
	
	public String getLabelResource() {
		return "Siptitle";
	}

	/**
	 * Implements JMeterGUIComponent.clearGui
	 */
	public void clearGui() {
		_logger.debug("SipSamplerConfigGui clear GUI");

		super.clearGui();
		if (table != null && table.getCellEditor() != null) {
			table.getCellEditor().stopCellEditing();
		}
		sipNodeName.setText("");
		
		GuiUtils.stopTableEditing(table);
		dataModel.setRowCount(0);
		
		bodyText.setText("");
		
		sipNodeName.setText("");
		
		sipDialogueSelector.setSelectedIndex(0);
		sipTransactionSelector.setSelectedIndex(0);
		sipRelatedTransactionSelector.setSelectedIndex(0);
		
		sendDirectionRadio.setSelected(true);
		requestRadio.setSelected(true);
		
		sipCommandSelector.setSelectedIndex(0);
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		_logger.debug("SipSamplerConfigGui create Test Element");

		SipSampler sipSampler = new SipSampler();
		modifyTestElement(sipSampler);
		return sipSampler;
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param element
	 *            the TestElement to configure
	 */
	public void configure(TestElement element) {
		
	    if (_logger.isDebugEnabled())
		_logger.debug("configure: element " + element.getName());

		super.configure(element);
		

		sendDirectionRadio.setSelected(true);		  
		
		sipNodeName.setText(element.getProperty("sip.node.name").getStringValue());
		
		String direction = element.getProperty("sip.message.direction").getStringValue();
		boolean dir = "send".equals(direction);
		sendDirectionRadio.setSelected(dir);
		receiveDirectionRadio.setSelected(!dir);
		sipRcvTimeout.setVisible(!dir);
		optionalRadio.setVisible(!dir);
		
        // resetRadio.setVisible(dir);
		resetRadio.setVisible(true);
		
		resetRadio.setSelected(element.getProperty("sip.message.reset").getBooleanValue());
		sipRcvTimeout.setText(element.getProperty("sip.message.timeout").getStringValue());
		optionalRadio.setSelected(element.getProperty("sip.message.optional").getBooleanValue());
		ignoreRetransmissionRadio.setSelected(element.getProperty("sip.message.ignoreRetransmission").getBooleanValue());
		
		String msgType = element.getProperty("sip.message.type").getStringValue();
		boolean isRequestType = "request".equals(msgType);
		ignoreRetransmissionRadio.setVisible(!dir && !isRequestType);
		requestRadio.setSelected(isRequestType);
		responseRadio.setSelected(!isRequestType);
		sipCommandPanel.setVisible(isRequestType); //sipCommandSelector.setVisible(isRequestType);
		responseCodePanel.setVisible(!isRequestType);
		sipCommandPanel.setBorder(BorderFactory.createTitledBorder("Which command"));

    	
		sipCommandSelector.setSelectedItem(element.getProperty("sip.message.command").getStringValue());
		responseCodeValue.setText(element.getProperty("sip.message.response.code").getStringValue());
		sipDialogueSelector.setSelectedItem(element.getProperty("sip.message.dialnb").getStringValue());
		sipTransactionSelector.setSelectedItem(element.getProperty("sip.message.transnb").getStringValue());
		sipRelatedTransactionSelector.setSelectedItem(element.getProperty("sip.message.relatedtransnb").getStringValue());

		DefaultTableModel rowModel =  dataModel; //(DefaultTableModel)table.getModel();
		
		dataModel.removeTableModelListener(this);
		
		int j = dataModel.getColumnCount();
		if (j != 2 ) {
			_logger.error("Sip Headers data model should have 2 columns instead of " + j);
		}
		dataModel.setRowCount(0);
		
		int i =0;
		while (element.getPropertyAsString("sip.header.name." + i) != null && !"".equals(element.getPropertyAsString("sip.header.name." + i))) {
			String[] data = {element.getPropertyAsString("sip.header.name." + i),element.getPropertyAsString("sip.header.value." + i)};
			dataModel.addRow(data);
			i++;
		}
		dataModel.addTableModelListener(this);
		
		bodyText.setText(element.getPropertyAsString("sip.body.text"));
		
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		
		_logger.debug("modifyTestElement");

		element.clear();

		this.configureTestElement(element);
		
		DefaultTableModel rowModel = dataModel;//(DefaultTableModel)table.getModel();
		
		dataModel.removeTableModelListener(this);
		
		int j = rowModel.getColumnCount();
		int i = rowModel.getRowCount();
		if (j != 2 ) {
			_logger.error("Sip Headers data model should have 2 columns instead of " + j);
		}
		
        element.setProperty("sip.node.name", sipNodeName.getText());
        if (sendDirectionRadio.isSelected()) {
        	element.setProperty("sip.message.direction", "send");
        } else {
        	element.setProperty("sip.message.direction", "receive");
        }

		element.setProperty("sip.message.reset", resetRadio.isSelected());
		element.setProperty("sip.message.timeout", sipRcvTimeout.getText());
		element.setProperty("sip.message.optional", optionalRadio.isSelected());
		element.setProperty("sip.message.ignoreRetransmission", ignoreRetransmissionRadio.isSelected());
		
        if (requestRadio.isSelected()) {
        	element.setProperty("sip.message.type", "request");
        } else {
        	element.setProperty("sip.message.type", "response");
        }

        element.setProperty("sip.message.command", getCommand());
        element.setProperty("sip.message.response.code", getResponseCode());
        element.setProperty("sip.message.dialnb", getDialogueNb());
        element.setProperty("sip.message.transnb", getTransactionNb());
        element.setProperty("sip.message.relatedtransnb", getRelatedTransactionNb());
        
		for (int k = 0; k < i; k++) {
			if (rowModel.getValueAt(k, 0) == null || "".equals(rowModel.getValueAt(k, 0))) {
				rowModel.setValueAt("EMPTY NAME !", k, 0);
			}
			element.setProperty("sip.header.name." + k, (String) rowModel.getValueAt(k, 0));
			element.setProperty("sip.header.value." + k, (String) rowModel.getValueAt(k, 1));
		}
		
		element.setProperty("sip.body.text", bodyText.getText());
		dataModel.addTableModelListener(this);
		
	}

	/***************************************************************************
	 * This will create a S6aCommand Panel in the MmeSamplerConfigGui
	 **************************************************************************/

	private void createSipNodeNamePanel(DynamicVerticalPanel vertPanel) {
		_logger.debug("SipSamplerConfigGui createSipNodePanel");

		VerticalPanel Panel = new VerticalPanel();
		Panel.setBorder(BorderFactory.createTitledBorder("Sip Node")); // $NON-NLS-1$

		HorizontalPanel horzPanel = new HorizontalPanel();

		JLabel MmeNameLabel = new JLabel("Sip Node Name"); // $NON-NLS-1$
		MmeNameLabel.setLabelFor(sipNodeName);
		horzPanel.add(MmeNameLabel);
		horzPanel.add(sipNodeName);

		Panel.add(horzPanel);
		sipCommandSelector.setEditable(false);
		sipCommandPanel.add(sipCommandSelector);
		Panel.add(sipCommandPanel, BorderLayout.WEST);

		vertPanel.add(Panel);
	}

	/**
	 * Refer to  CR7623.
	 * @param vertPanel
	 */
	protected void createDirectionPanel(DynamicVerticalPanel vertPanel) {
		_logger.debug("SipSamplerConfigGui createDirectionPanel");

	    VerticalPanel directionPanel = new VerticalPanel();
        directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));
        
        ButtonGroup group = new ButtonGroup();
        group.add(sendDirectionRadio);
        group.add(receiveDirectionRadio);

        
        sendDirectionRadio.addActionListener(this);
        receiveDirectionRadio.addActionListener(this);
        
        HorizontalPanel horzPanel = new HorizontalPanel();
        HorizontalPanel horzPanelLeft = new HorizontalPanel();
        HorizontalPanel horzPanelRight = new HorizontalPanel();
        horzPanelLeft.add(sendDirectionRadio); 
        horzPanelLeft.add(receiveDirectionRadio); 
        
        sipRcvTimeout.setBorder(BorderFactory.createTitledBorder("reception timeout"));
        horzPanelRight.add(optionalRadio);
        horzPanelRight.add(ignoreRetransmissionRadio);
        horzPanelRight.add(sipRcvTimeout);
        horzPanelRight.add(resetRadio);        
        horzPanel.add(horzPanelLeft);
        horzPanel.add(horzPanelRight);
        
        directionPanel.add(horzPanel);
        vertPanel.add(directionPanel);        
    }
	
	
	
    protected void createControlPanel(DynamicVerticalPanel vertPanel) {
		_logger.debug("SipSamplerConfigGui createControlPanel");

        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.add(createTitleLabel());
        HorizontalPanel headerPanel = new HorizontalPanel() ;
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        titlePanel.add(headerPanel);
        
        
        HorizontalPanel contentPanel = new HorizontalPanel();
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        
        for (JButton button : buttons) {
            contentPanel.add(button);        	
			button.addActionListener(this);
        }
        
        sipHeaderSelector.setEditable(false);
        sipHeaderSelector.addItem("");
        if (dico != null)
        for (Header item : dico) {
            sipHeaderSelector.addItem(item.getHeaderName());
		}
        contentPanel.add(sipHeaderSelector);
        
        titlePanel.add(contentPanel);
        vertPanel.add(titlePanel);        
    }
	    
	/**
	 * This will create the Add test panel in the LdapConfigGui.
	 */
	private void createTreePanel(DynamicVerticalPanel vertPanel) {
		_logger.debug("createTreePanel ");

		// Set some callbacks
		
		JPanel avpDetailPanel2 = new JPanel(new BorderLayout(5, 0));

		dataModel = new DefaultTableModel(0,2);
		String[] columnNames = {"Header name","Header value"};
		dataModel.setColumnIdentifiers(columnNames);
		table = new JTable(dataModel);
		dataModel.addTableModelListener(this);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
		//table.
		JScrollPane scrollPane2 = new JScrollPane(table);
		avpDetailPanel2.add(scrollPane2, BorderLayout.CENTER);
		
		vertPanel.add(avpDetailPanel2);
		vertPanel.add(bodyLabel);
		vertPanel.add(bodyText);
		
	}

	/**
	 * Refer to  CR7623.
	 * @param vertPanel
	 */
	protected void createRequestResponsePanel(HorizontalPanel vertPanel) {
	
		_logger.debug("SipSamplerConfigGui createRequestResponsePanel");

	    VerticalPanel directionPanel = new VerticalPanel();
        directionPanel.setBorder(BorderFactory.createTitledBorder("IsRequest"));
        
        ButtonGroup group = new ButtonGroup();
        group.add(requestRadio);
        group.add(responseRadio);
        requestRadio.addActionListener(this);
        responseRadio.addActionListener(this);
        
        HorizontalPanel horzPanel = new HorizontalPanel();
        horzPanel.add(requestRadio); 
        horzPanel.add(responseRadio); 
        
        directionPanel.add(horzPanel);
        vertPanel.add(directionPanel);
    }


	private void createResponseCodePanel(HorizontalPanel vertPanel) {
		_logger.debug("createresponseCodePanel ");
	    responseCodePanel.setBorder(BorderFactory.createTitledBorder("Which response code"));
	    responseCodePanel.add(responseCodeValue);
        vertPanel.add(responseCodePanel);
	}
	
	
	private void createCommandPanel(DynamicVerticalPanel vertPanel) {
		_logger.debug("createCommandPanel ");
		//DynamicVerticalPanel commandPanel = new DynamicVerticalPanel();
		HorizontalPanel commandPanel = new HorizontalPanel();
		
		createRequestResponsePanel(commandPanel);
		createResponseCodePanel(commandPanel);
		
		for (Field sipMethod : Request.class.getDeclaredFields()) {
			sipCommandSelector.addItem(sipMethod.getName());
		}

		// Change the command layout on combo change
		sipCommandSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
  		    	  	onSipCommandChange((JComboBox) e.getSource());
                }
            }
        });
		
		VerticalPanel sipDialogPanel = new VerticalPanel();
		sipDialogPanel.setBorder(BorderFactory.createTitledBorder("Dialog ID"));
		for (DialogNb oneDialNb : DialogNb.values()) {
			sipDialogueSelector.addItem(oneDialNb.name());			
		}
		sipDialogueSelector.setEditable(true);
		sipDialogPanel.add(sipDialogueSelector);

		VerticalPanel sipTransactionPanel = new VerticalPanel();
		sipTransactionPanel.setBorder(BorderFactory.createTitledBorder("Transaction ID"));
		for (TransactionNb oneTransNb : TransactionNb.values()) {
			sipTransactionSelector.addItem(oneTransNb.name());			
		}
		sipTransactionPanel.add(sipTransactionSelector);

		sipRelatedTransactionSelector.addItem("None");
		for (TransactionNb oneTransNb : TransactionNb.values()) {
			sipRelatedTransactionSelector.addItem(oneTransNb.name());
		}
		
		VerticalPanel sipRelatedTransactionPanel = new VerticalPanel();
		sipRelatedTransactionPanel.setBorder(BorderFactory.createTitledBorder("Related transaction"));
		sipRelatedTransactionPanel.add(sipRelatedTransactionSelector);
		
		commandPanel.add(sipCommandPanel);
		commandPanel.add(sipDialogPanel);
		commandPanel.add(sipTransactionPanel);
		commandPanel.add(sipRelatedTransactionPanel);
		vertPanel.add(commandPanel);
	}

	private void onSipCommandChange(JComboBox cbxCommand) {
		_logger.debug("onSipCommandChange ");
	}

	public void valueChanged(TreeSelectionEvent e) {
		_logger.debug("valueChanged " + e.toString());
		saveTreeValue = false;
		
		TreePath oldPath = e.getOldLeadSelectionPath();

		currentLabel.setText("label1");
		currentValue.setText("value1");

		TreePath path = e.getNewLeadSelectionPath();

		mainPanel.revalidate();
		
		saveTreeValue = true;
	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		_logger.debug("actionPerformed " + actionCommand);
		
		if ("Add".equals(actionCommand)) {
			DefaultTableModel rowModel = (DefaultTableModel)table.getModel();
			
			String [] row={(String) sipHeaderSelector.getSelectedItem(),""};
			rowModel.addRow(row);
			rowModel.fireTableDataChanged();
			table.addRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
		} else 
		if ("Remove".equals(actionCommand)) {
				DefaultTableModel rowModel = (DefaultTableModel)table.getModel();
				
				int[] rows = table.getSelectedRows();
				for(int i=0; i<rows.length; i++){
					rowModel.removeRow(rows[i]-i);
	            }
				
				rowModel.fireTableDataChanged();
		} else 
		if ("Up".equals(actionCommand)) {
			DefaultTableModel rowModel = (DefaultTableModel)table.getModel();
			
			int[] rowsSelected = table.getSelectedRows();
			if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
                table.clearSelection();
                for (int rowSelected : rowsSelected) {
                	rowModel.moveRow(rowSelected, rowSelected, rowSelected - 1);
                	table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
                }
            }
		} else 
		if ("Down".equals(actionCommand)) {
				DefaultTableModel rowModel = (DefaultTableModel)table.getModel();
				
				int[] rowsSelected = table.getSelectedRows();
				if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
	                table.clearSelection();
	                for (int i = rowsSelected.length - 1; i >= 0; i--) {
	                    int rowSelected = rowsSelected[i];
	                    rowModel.moveRow(rowSelected, rowSelected, rowSelected + 1);
	                    table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
	                }
	            }
		} else 
		if ("Request".equals(actionCommand)) {
			sipCommandPanel.setVisible(true);
			responseCodePanel.setVisible(false);
		} else 
		if ("Response".equals(actionCommand)) {
			sipCommandPanel.setVisible(false);
			responseCodePanel.setVisible(true);
		} else 
			if ("Send".equals(actionCommand)) {
				sipRcvTimeout.setVisible(false);
				optionalRadio.setVisible(false);
				ignoreRetransmissionRadio.setVisible(false);
		        //resetRadio.setVisible(true);		
				resetRadio.setVisible(true);
		} else 
			if ("Receive".equals(actionCommand)) {
				sipRcvTimeout.setVisible(true);
				optionalRadio.setVisible(true);
				ignoreRetransmissionRadio.setVisible(true);
				resetRadio.setVisible(true);
		}

	}

	public DefaultTableModel getHeadersTableModel() {
		DefaultTableModel rowModel = (DefaultTableModel)table.getModel();
		return rowModel;
	}

	public String getCommand() {
		return sipCommandSelector.getSelectedItem().toString();
	}

	public String getResponseCode() {
		return responseCodeValue.getText();
	}
	
	public String getDialogueNb() {		
		return sipDialogueSelector.getSelectedItem().toString();
	}

	public String getTransactionNb() {
		return sipTransactionSelector.getSelectedItem().toString();
	}

	public String getRelatedTransactionNb() {
		return sipRelatedTransactionSelector.getSelectedItem().toString();
	}

	// ------------------------------------------
	//           COMMAND PANEL
	// ------------------------------------------
	
	public String getBodyText() {
		return bodyText.getText();
	}


	public void showCommandPanel() {
        buttons[0].setEnabled(true);
        buttons[1].setEnabled(true);
        buttons[2].setEnabled(false);
        buttons[3].setEnabled(false);
	}

	public void hideCommandPanel() {
        buttons[0].setEnabled(true);
        buttons[1].setEnabled(true);
        buttons[2].setEnabled(false);
        buttons[3].setEnabled(false);
	}
	
    protected JLabel createTitleLabel() {
        Font curFont = avpLabel.getFont();
        avpLabel.setFont(curFont.deriveFont((float) curFont.getSize() + 2));
        return avpLabel;
    }

    public void tableChanged(TableModelEvent e) {
    	_logger.debug("tableChanged ");
		table.repaint();
    }

}
