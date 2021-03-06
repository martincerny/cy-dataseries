package cz.cas.mbu.cydataseries.internal.ui;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import cz.cas.mbu.cydataseries.dataimport.PreImportResults;
import cz.cas.mbu.cydataseries.internal.dataimport.DataSeriesImportParameters;
import cz.cas.mbu.cydataseries.internal.dataimport.ImportHelper;
import cz.cas.mbu.cydataseries.internal.dataimport.SoftFile.SoftTable;
import cz.cas.mbu.cydataseries.internal.dataimport.SoftFileImportParameters;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;

public class SoftFileImportParametersPanel extends JPanel {

	private final IndexImportOptionsPanel indexImportOptionsPanel;
	private final SelectColumnsToImportPanel columnsToImportPanel;
	private final ImportPreviewPanel previewPanel;
	
	private WeakReference<SoftTable> sourceTable;
	
	private PreImportResults lastPreviewResults;
	
	private final Logger userLogger = Logger.getLogger(CyUserLog.NAME); 
	private JPanel panel;
	private JPanel panel_1;
	
	/**
	 * Create the panel.
	 */
	public SoftFileImportParametersPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Index options", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(panel, "2, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		indexImportOptionsPanel = new IndexImportOptionsPanel();
		panel.add(indexImportOptionsPanel, "1, 1, fill, fill");
		indexImportOptionsPanel.addChangedListener(evt -> updatePreview());
		indexImportOptionsPanel.setManualAddVisible(false);
		
		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Columns to import", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(panel_1, "4, 2, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		columnsToImportPanel = new SelectColumnsToImportPanel();
		panel_1.add(columnsToImportPanel, "1, 1, fill, fill");
		columnsToImportPanel.addChangeListener(evt -> updatePreview());
		
		previewPanel = new ImportPreviewPanel();
		add(previewPanel, "2, 4, 3, 1, fill, fill");

	}
	
	public void setData(SoftTable table)
	{
		this.sourceTable = new WeakReference<>(table);
		columnsToImportPanel.setAvailableColumns(table.getColumnNames(), table.getColumnDescriptions());
		updatePreview();
	}
	
	protected void updatePreview()
	{
		try {
			DataSeriesImportParameters dataSeriesImportParameters = getDataSeriesImportParameters();
			lastPreviewResults = ImportHelper.preImportFromArrayAndIndex(sourceTable.get().getColumnNames(), sourceTable.get().getContents(), dataSeriesImportParameters, false /*strict*/);
			previewPanel.updatePreview(lastPreviewResults, dataSeriesImportParameters, false, false);
		}
		catch (Exception ex)
		{
			userLogger.error("Error creating import preview", ex);
			previewPanel.showError(ex.getClass().getSimpleName() + ": " + ex.getMessage(), null);			
		}
	}
	
	public DataSeriesImportParameters getDataSeriesImportParameters()
	{
		DataSeriesImportParameters value = new DataSeriesImportParameters();
		value.setIndexSource(indexImportOptionsPanel.getIndexSource());
		List<String> manualIndexValues = indexImportOptionsPanel.getManualIndexValues();
		value.setManualIndexValues(manualIndexValues);
		value.setImportRowNames(true);
		value.setImportAllColumns(columnsToImportPanel.isImportAllColumns());
		value.setImportedColumnIndices(columnsToImportPanel.getImportedColumnIndices());		
		return value;
	}
	

	public SoftFileImportParameters getSofFileImportParameters()
	{
		SoftFileImportParameters value = new SoftFileImportParameters();
		value.setDataSeriesImportParameters(getDataSeriesImportParameters());
		value.setSelectedTable(sourceTable.get());
		return value;
	}
}
