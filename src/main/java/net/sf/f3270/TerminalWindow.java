package net.sf.f3270;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.h3270.host.Field;
import org.h3270.host.InputField;
import org.h3270.host.S3270;

public class TerminalWindow {
	
	private static final String MASKED_VALUE = "****";
    private S3270 s3270;
	private int currentWidth;
	private int currentHeight;

	private Style styleInputChanged;
	private Style styleInput;
	private Style styleHidden;
	private Style styleBlack;

	private Style styleCommand;
	private Style stylePunctuation;
	private Style styleReturn;
	private Style styleParamName;
	private Style styleParamValue;

	private final Font monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN,
			12);
	private final Font sansFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	private Color[] extendedColors = new Color[] { Color.cyan, Color.blue,
			Color.red, Color.pink, Color.green, Color.magenta, Color.yellow,
			new Color(198, 198, 198) };

	private Map<String, Style> stylesFlyweight = new HashMap<String, Style>();

	private JFrame frame;
	private JTextPane textPane3270;
	private JTextPane textPaneDebug;
	private DefaultStyledDocument documentDebug;

	private JTable fieldsTable;
	private JTabbedPane tabbedPane;

	public TerminalWindow(final S3270 s3270) {
		this.s3270 = s3270;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		initializeStyles();
		createFrame(s3270.getHostname());
	}

	private void initializeStyles() {
		styleInputChanged = createStyle(Color.black, Color.red, false);
		styleInput = createStyle(Color.green, Color.black, false);
		styleHidden= createStyle(Color.black, Color.black, false);
		styleCommand = createStyle(Color.black, Color.white, false);
		stylePunctuation = createStyle(Color.gray, Color.white, false);
		styleReturn = createStyle(Color.magenta, Color.white, false);
		styleParamName = createStyle(new Color(128, 0, 0), Color.white, false);
		styleParamValue = createStyle(Color.blue, Color.white, false);
	}

	public void update(final String command, final String returned,
			final Parameter... parameters) {
		updateTerminal();
		updateDebug(command, returned, parameters);
		updateFieldsTable();
	}

	private void updateTerminal() {
		final DefaultStyledDocument doc = new DefaultStyledDocument();
		for (Field f : s3270.getScreen().getFields()) {
			final Style s = getStyle(f);
			final String text = f.getText().replace('\u0000', ' ');
			if ((f instanceof InputField) && text.startsWith(" ")) {
				appendText(doc, " ", styleBlack);
				appendText(doc, text.substring(1), s);
			} else {
				appendText(doc, text, s);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					boolean sizeChanged = updateTextPane3270Size();
					if (sizeChanged) {
						updateTabbedPaneSize();
						frame.pack();
					}
					textPane3270.setDocument(doc);
				} catch (RuntimeException e) {
					// do nothing
				}
			}
		});
	}

	private void updateDebug(final String command, final String returned,
			final Parameter... parameters) {
		if (documentDebug.getLength() > 0) {
			appendText(documentDebug, "\n", stylePunctuation);
		}
		appendText(documentDebug, command, styleCommand);
		appendText(documentDebug, "(", stylePunctuation);
		for (int i = 0; i < parameters.length; i++) {
			appendText(documentDebug, parameters[i].getName(), styleParamName);
			appendText(documentDebug, "=", stylePunctuation);
			appendText(documentDebug, parameters[i].getValue(), styleParamValue);
			if (i != parameters.length - 1) {
				appendText(documentDebug, ", ", stylePunctuation);
			}
		}
		appendText(documentDebug, ")", stylePunctuation);
		if (returned != null) {
			appendText(documentDebug, " = ", stylePunctuation);
			appendText(documentDebug, "\"" + returned + "\"", styleReturn);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textPaneDebug.scrollRectToVisible(new Rectangle(0,
						textPaneDebug.getHeight() * 2, 1, 1));
			}
		});
	}

	private void updateFieldsTable() {
		((AbstractTableModel) fieldsTable.getModel()).fireTableDataChanged();
	}

	private Style getStyle(final Field f) {
		final boolean isInput = f instanceof InputField;
        if (f.isHidden()) {
            return styleHidden;   
        }
        
		if (isInput) {
			final InputField inputField = (InputField) f;
			if (inputField.isChanged()) {
				return styleInputChanged;
			} else {
				return styleInput;
			}
		}

		final int i = (f.getExtendedColor() == 0) ? 0
				: f.getExtendedColor() - 0xf0;
		Color foregroundColor = extendedColors[i];
		Color backgroundColor = Color.black;
		if (f.getExtendedHighlight() == Field.ATTR_EH_REV_VIDEO) {
			final Color tmp = backgroundColor;
			backgroundColor = foregroundColor;
			foregroundColor = tmp;
		}
		boolean isUnderline = f.getExtendedHighlight() == Field.ATTR_EH_UNDERSCORE;

		if (f.isIntensified()) {
			foregroundColor = Color.white;
		}
		return createStyle(foregroundColor, backgroundColor, isUnderline);
	}

	private void appendText(final DefaultStyledDocument doc, final String text,
			final Style style) {
		try {
			doc.insertString(doc.getLength(), text, style);
		} catch (final BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private void createFrame(final String title) {

		buildTextPane3270();
		final JScrollPane tableScroller = buildFieldsTablePanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Terminal", null, textPane3270, "");
		tabbedPane.addTab("Fields", null, tableScroller, "");
		updateTabbedPaneSize();

		final JPanel debugPanel = buildDebugPanel(monospacedFont, sansFont);

		frame = new JFrame(title);

		final Container contentPane = frame.getContentPane();
		contentPane.setBackground(new Color(224, 224, 224));
		contentPane.add(tabbedPane, BorderLayout.NORTH);
		contentPane.add(debugPanel, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}

	private void updateTabbedPaneSize() {
		tabbedPane.setPreferredSize(new Dimension((int) textPane3270
				.getPreferredSize().getWidth() + 40, (int) textPane3270
				.getPreferredSize().getHeight() + 40));
	}

	private JPanel buildDebugPanel(final Font monospacedFont,
			final Font sansFont) {
		final JScrollPane textPaneDebugScroller = buildTextPaneDebug();

		documentDebug = new DefaultStyledDocument();
		textPaneDebug.setDocument(documentDebug);

		final JPanel debugPanel = new JPanel();
		final BoxLayout boxLayout = new BoxLayout(debugPanel,
				BoxLayout.PAGE_AXIS);
		debugPanel.setLayout(boxLayout);
		debugPanel.setBackground(new Color(224, 224, 224));
		debugPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
		debugPanel.add(textPaneDebugScroller);
		return debugPanel;
	}

	private JScrollPane buildTextPaneDebug() {
		textPaneDebug = createTextPane(sansFont, Color.white);
		textPaneDebug.setAutoscrolls(true);
		textPaneDebug.setBorder(new EmptyBorder(3, 3, 3, 3));
		final FontMetrics fontMetricsSans = textPane3270
				.getFontMetrics(monospacedFont);
		final JScrollPane textPaneDebugScroller = new JScrollPane(textPaneDebug);
		textPaneDebugScroller.setPreferredSize(new Dimension(textPane3270
				.getWidth(), 3 + 10 * fontMetricsSans.getHeight()));
		textPaneDebugScroller.setAlignmentX(JDialog.LEFT_ALIGNMENT);
		textPaneDebugScroller.setBorder(new LineBorder(Color.gray));
		textPaneDebugScroller.setAutoscrolls(true);
		return textPaneDebugScroller;
	}

	private void buildTextPane3270() {
		textPane3270 = createTextPane(monospacedFont, Color.black);
		updateTextPane3270Size();
		textPane3270.setAlignmentX(JDialog.LEFT_ALIGNMENT);
	}

	private boolean updateTextPane3270Size() {
		final FontMetrics fontMetricsMonospaced = textPane3270
				.getFontMetrics(monospacedFont);
		int w = s3270.getScreen().getWidth();
		int h = s3270.getScreen().getHeight();
		if (w != currentWidth || h != currentHeight) {
			textPane3270.setPreferredSize(new Dimension((w + 2)
					* fontMetricsMonospaced.charWidth(' '), (h + 2)
					* fontMetricsMonospaced.getHeight()));
			currentWidth = w;
			currentHeight = h;
			return true;
		}
		return false;
	}

	private JScrollPane buildFieldsTablePanel() {
		fieldsTable = new JTable(new AbstractTableModel() {
			private static final long serialVersionUID = 5347188337180793036L;
			private String[] columnNames = new String[] { "Id", "Type", "Value" };

			public int getColumnCount() {
				return 3;
			}

			public int getRowCount() {
				try {
					return s3270.getScreen().getFields().size();
				} catch (Exception e) {
					return 0;
				}
			}

			@Override
			public String getColumnName(final int column) {
				return columnNames[column];
			}

			public Object getValueAt(final int rowIndex, final int columnIndex) {
				if (columnIndex == 0) {
					return rowIndex;
				}
				Field f;
				try {
					f = s3270.getScreen().getFields().get(rowIndex);
				} catch (Exception e) {
					// nasty hack to handle some random not connected exceptions from s3270
					return "";
				}
				if (columnIndex == 1) {
					return ((f instanceof InputField) ? "in" : "out")
							+ (((f instanceof InputField) && ((InputField) f)
									.isChanged()) ? " *" : "");
				}
				if (columnIndex == 2) {
					return getMaskedValueIfFieldIsHidden(f);
				}
				throw new RuntimeException("unknown column index "
						+ columnIndex);
			}

            private String getMaskedValueIfFieldIsHidden(Field f) {
                String value = f.isHidden() ? MASKED_VALUE : f.getValue().replace('\u0000', ' ');
                return "[" + value + "]";
            }

			public boolean isCellEditable(final int rowIndex,
					final int columnIndex) {
				return columnIndex == 2;
			}
		});

		fieldsTable.getColumnModel().getColumn(0).setPreferredWidth(25);
		fieldsTable.getColumnModel().getColumn(1).setPreferredWidth(35);
		fieldsTable.getColumnModel().getColumn(2).setPreferredWidth(600);
		// fieldsTable.setAutoCreateRowSorter(true);

		final JScrollPane tableScroller = new JScrollPane(fieldsTable);
		return tableScroller;
	}

	private JTextPane createTextPane(final Font font, final Color color) {
		final JTextPane textPane = new JTextPane();
		textPane.setFont(font);
		textPane.setBackground(color);
		textPane.setEditable(false);
		return textPane;
	}

	private Style createStyle(final Color foregroundColor,
			final Color backgrondColor, final boolean isItalic) {
		final String key = String.format("%d-%d-%d %d-%d-%d %s",
				foregroundColor.getRed(), foregroundColor.getGreen(),
				foregroundColor.getBlue(), backgrondColor.getRed(),
				backgrondColor.getGreen(), backgrondColor.getBlue(), isItalic);

		Style style = stylesFlyweight.get(key);
		if (style == null) {
			style = StyleContext.getDefaultStyleContext().addStyle(null, null);
			StyleConstants.setForeground(style, foregroundColor);
			StyleConstants.setBackground(style, backgrondColor);
			StyleConstants.setItalic(style, isItalic);
			stylesFlyweight.put(key, style);
		}

		return style;
	}

	public void close() {
		frame.setVisible(false);
	}

}