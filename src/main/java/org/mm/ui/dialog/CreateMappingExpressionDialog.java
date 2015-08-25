package org.mm.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mm.core.MappingExpression;
import org.mm.exceptions.MappingMasterException;
import org.mm.ss.SpreadSheetUtil;
import org.mm.ui.model.DataSourceModel;
import org.mm.ui.model.MappingsExpressionsModel;
import org.mm.ui.view.ApplicationView;

public class CreateMappingExpressionDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private ApplicationView container;

	private boolean editMode = false;
	private MappingExpression selectedMapping;

	private JComboBox<String> cbbSheetName;

	private JTextField txtStartColumn;
	private JTextField txtEndColumn;
	private JTextField txtStartRow;
	private JTextField txtEndRow;
	private JTextField txtComment;

	private JTextArea txtExpression;

	public CreateMappingExpressionDialog(ApplicationView container)
	{
		this.container = container;

		setTitle("MappingMaster Expression Dialog");
		setLocationRelativeTo(container);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		contentPane.add(pnlMain, BorderLayout.CENTER);

		JLabel lblSheetName = new JLabel("Sheet name*:");
		cbbSheetName = new JComboBox<>();
		cbbSheetName.setModel(new DefaultComboBoxModel<>(new Vector<>(getDataSourceModel().getSheetNames())));

		JLabel lblStartColumn = new JLabel("Start column*:");
		txtStartColumn = new JTextField("");

		JLabel lblEndColumn = new JLabel("End column*:");
		txtEndColumn = new JTextField("");

		JLabel lblStartRow = new JLabel("Start row*:");
		txtStartRow = new JTextField("");

		JLabel lblEndRow = new JLabel("End row*:");
		txtEndRow = new JTextField("");

		JLabel lblComment = new JLabel("Comment:");
		txtComment = new JTextField("");

		JLabel lblExpression = new JLabel("DSL mapping expression*:");

		JPanel pnlFields = new JPanel(new GridLayout(7, 2));
		pnlFields.add(lblSheetName);
		pnlFields.add(cbbSheetName);
		pnlFields.add(lblStartColumn);
		pnlFields.add(txtStartColumn);
		pnlFields.add(lblEndColumn);
		pnlFields.add(txtEndColumn);
		pnlFields.add(lblStartRow);
		pnlFields.add(txtStartRow);
		pnlFields.add(lblEndRow);
		pnlFields.add(txtEndRow);
		pnlFields.add(lblComment);
		pnlFields.add(txtComment);
		pnlFields.add(lblExpression);

		pnlMain.add(pnlFields, BorderLayout.NORTH);

		txtExpression = new JTextArea("", 20, 48);
		pnlMain.add(txtExpression, BorderLayout.CENTER);

		JPanel pnlCommands = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton cmdCancel = new JButton("Cancel");
		cmdCancel.setPreferredSize(new Dimension(100, 30));
		cmdCancel.addActionListener(new CancelActionListener());

		JButton cmdOK = new JButton("Save Changes");
		cmdOK.setPreferredSize(new Dimension(150, 30));
		cmdOK.addActionListener(new SaveChangesActionListener());

		pnlCommands.add(cmdCancel);
		pnlCommands.add(cmdOK);

		pnlMain.add(pnlCommands, BorderLayout.SOUTH);

		pack();
	}

	public void fillDialogFields(MappingExpression mapping)
	{
		String sheetName = mapping.getSheetName();

		if (getDataSourceModel().isEmpty()) {
			List<String> sheetNames = getDataSourceModel().getSheetNames();
			sheetNames.forEach(cbbSheetName::addItem);
			if (!sheetNames.contains(sheetName)) {
				cbbSheetName.addItem(sheetName);
			}
		}
		else {
			cbbSheetName.addItem(mapping.getSheetName());
		}
		cbbSheetName.setSelectedItem(sheetName);

		txtStartColumn.setText(mapping.getStartColumn());
		txtEndColumn.setText(mapping.getEndColumn());
		txtStartRow.setText(mapping.getStartRow());
		txtEndRow.setText(mapping.getEndRow());

		txtComment.setText(mapping.getComment());
		txtExpression.setText(mapping.getExpressionString());

		editMode = true;
		selectedMapping = mapping;
	}

	private class CancelActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}

	private class SaveChangesActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try {
				String sheetName = (String) cbbSheetName.getSelectedItem();

				String startColumn = txtStartColumn.getText().trim().toUpperCase();
				String endColumn = txtEndColumn.getText().trim().toUpperCase();
				SpreadSheetUtil.checkColumnSpecification(startColumn);
				SpreadSheetUtil.checkColumnSpecification(endColumn);
				
				String startRow = txtStartRow.getText().trim();
				String endRow = txtEndRow.getText().trim();
				
				String comment = txtComment.getText().trim();
				String expression = txtExpression.getText().trim();
				
				MappingExpression newMapping = new MappingExpression(comment, expression, sheetName, startColumn, endColumn, startRow, endRow);
				if (editMode) {
					getMappingExpressionsModel().removeMappingExpression(selectedMapping); // Remove original
				}
				getMappingExpressionsModel().addMappingExpression(newMapping);
				updateMappingBrowserView();
				setVisible(false);
			}
			catch (MappingMasterException ex) {
				getApplicationDialogManager().showErrorMessageDialog(container, ex.getMessage());
			}
		}

		private void updateMappingBrowserView()
		{
			container.getMappingBrowserView().update();
		}
	}

	private MappingsExpressionsModel getMappingExpressionsModel()
	{
		return container.getApplicationModel().getMappingExpressionsModel();
	}

	private DataSourceModel getDataSourceModel()
	{
		return container.getApplicationModel().getDataSourceModel();
	}

	private MMApplicationDialogManager getApplicationDialogManager()
	{
		return container.getApplicationDialogManager();
	}
}
