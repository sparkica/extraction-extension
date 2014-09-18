package com.brainymachine.extraction.operations;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;

import com.brainymachine.extraction.operations.ExtractionChange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import com.google.refine.history.Change;
import com.google.refine.model.Cell;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.model.changes.CellAtRow;
import com.google.refine.model.changes.ColumnAdditionChange;
import com.google.refine.model.changes.ColumnRemovalChange;
import com.google.refine.util.JSONUtilities;
import com.google.refine.util.Pool;

/**
 * A change resulting from extracting HTML element
 * @author Mateja Verlic
 */
public class ExtractionChange implements Change {
    private final int columnIndex;
    private final String[] serviceNames;
    private final String[] columnNames;
    private final String[][][] extractedElements;
    private final List<Integer> addedRowIds;
    
    /**
     * Creates a new <tt>ExtractionChange</tt>
     * @param columnIndex The index of the column used for element extraction
     * @param serviceNames The names of the used services
     * @param extractedElements The extracted elements per row and service
     */
    public ExtractionChange(final int columnIndex, final String[] serviceNames, final String[] columnNames, final String[][][] extractedElements) {
        this.columnIndex = columnIndex;
        this.serviceNames = serviceNames;
        this.columnNames = columnNames;
        this.extractedElements = extractedElements;
        this.addedRowIds = new ArrayList<Integer>();
    }

    /** {@inheritDoc} */
    @Override
    public void apply(final Project project) {
        synchronized(project) {
            final int[] cellIndexes = createColumns(project);
            insertValues(project, cellIndexes);
            project.update();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void revert(final Project project) {
        synchronized(project) {
            deleteRows(project);
            deleteColumns(project);
            project.update();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save(final Writer writer, final Properties options) throws IOException {
        final JSONWriter json = new JSONWriter(writer);
        try {
            /* Change object */
            json.object();
            json.key("column"); json.value(columnIndex);
            json.key("services"); JSONUtilities.writeStringArray(json, serviceNames);
            json.key("columns"); JSONUtilities.writeStringArray(json, columnNames);
            json.key("elements");
            /* Named entities nested array */
            {
                /* Rows array */
                json.array();
                for (final String[][] row : extractedElements) {
                    /* Services array */
                    json.array();
                    /* Service results array */
                    for (final String[] entities : row) {
                        json.array();
                        for (final String element : entities) {
                                json.object();
                                json.key("extractedText"); 
                                json.value(element);
                                json.endObject();
                        }
                        json.endArray();
                    }
                    json.endArray();
                }
                json.endArray();
            }
            json.key("addedRows");
            /* Added row numbers array */
            {
                json.array();
                for (Integer addedRowId : addedRowIds)
                    json.value(addedRowId.intValue());
                json.endArray();
            }
            json.endObject();
        }
        catch (JSONException error) {
            throw new IOException(error);
        }
    }
    
    /**
     * Create a <tt>ExtractionChange</tt> from a configuration reader
     * @param reader The reader
     * @param pool (unused but required, since this method is called through reflection)
     * @return A new <tt>ExtractionChange</tt>
     * @throws Exception If the configuration is in an unexpected format
     */
    static public Change load(LineNumberReader reader, Pool pool) throws Exception {
        /* Parse JSON line */
        final JSONTokener tokener = new JSONTokener(reader.readLine());
        final JSONObject changeJson = (JSONObject)tokener.nextValue();
        
        /* Simple properties */
        final int columnIndex = changeJson.getInt("column");
        final String[] serviceNames = JSONUtilities.getStringArray(changeJson, "services");
        final String[] columnNames = JSONUtilities.getStringArray(changeJson, "columns");
        
        /* Named entities nested array */
        final JSONArray extractedElementsJson = changeJson.getJSONArray("elements");
        final String[][][] extractedElements = new String[extractedElementsJson.length()][][];
        /* Rows array */
        for (int i = 0; i < extractedElements.length; i++) {
            /* Services array */
            final JSONArray serviceResultsJson = extractedElementsJson.getJSONArray(i);
            final String[][] serviceResults = extractedElements[i] = new String[serviceResultsJson.length()][];
            for (int j = 0; j < serviceResults.length; j++) {
                /* Service results array */
                final JSONArray entitiesJson = serviceResultsJson.getJSONArray(j);
                final String[] entities = serviceResults[j] = new String[serviceResultsJson.length()];
                for (int k = 0; k < entities.length; k++)
                    entities[k] = new String(entitiesJson.getJSONObject(k).getString("extractedText"));
            }
        }
        
        /* Reconstruct change object */
        final ExtractionChange change = new ExtractionChange(columnIndex, serviceNames, columnNames, extractedElements);
        for (final int addedRowId : JSONUtilities.getIntArray(changeJson, "addedRows"))
            change.addedRowIds.add(addedRowId);
        return change;
    }
    
    /**
     * Create the columns where the named entities will be stored
     * @param project The project
     * @return The cell indexes of the created columns
     */
    protected int[] createColumns(final Project project) {
        // Create empty cells that will populate each row
        final int rowCount = project.rows.size();
        final ArrayList<CellAtRow> emptyCells = new ArrayList<CellAtRow>(rowCount);
        for (int r = 0; r < rowCount; r++)
            emptyCells.add(new CellAtRow(r, null));
        
        // Create rows
        final int[] cellIndexes = new int[serviceNames.length];
        for (int c = 0; c < serviceNames.length; c++) {
            final CustomColumnAdditionChange change
                  = new CustomColumnAdditionChange(columnNames[c], columnIndex + c, emptyCells);
            change.apply(project);
            cellIndexes[c] = change.getCellIndex();
        }
        // Return cell indexes of created rows
        return cellIndexes;
    }
    
    /**
     * Delete the columns where the extracted elements have been stored
     * @param project The project
     */
    protected void deleteColumns(final Project project) {
        for (int i = 0; i < serviceNames.length; i++)
            new ColumnRemovalChange(columnIndex).apply(project);
    }

    /**
     * Insert the extracted elements into rows with the specified cell indexes
     * @param project The project
     * @param cellIndexes The cell indexes of the rows that will contain the extracted elements
     */
    protected void insertValues(final Project project, final int[] cellIndexes) {
        final List<Row> rows = project.rows;
        // Make sure there are rows
        if (rows.isEmpty())
            return;
        
        // Make sure all rows have enough cells, creating new ones as necessary
        final int maxCellIndex = Collections.max(Arrays.asList(ArrayUtils.toObject(cellIndexes)));
        final int minRowSize = maxCellIndex + 1;
        for (final Row row : rows)
            while (row.cells.size() < minRowSize)
                row.cells.add(null);
        
        // Add the extracted named entities to all rows, creating new ones as necessary
        int rowNumber = 0;
        addedRowIds.clear();
        for (final String[][] serviceElements : extractedElements) {
            // Determine the maximum number of named entities per service
            int maxElements = 0;
            for (final String[] entities : serviceElements)
                maxElements = Math.max(maxElements, entities.length);
            // Skip this row if no named entities were found
            if (maxElements == 0) {
                rowNumber++;
                continue;
            }
            // Create new blank rows if named entities don't fit on a single line
            for (int i = 1; i < maxElements; i++) {
                final Row elementRow = new Row(minRowSize);
                final int elementRowId = rowNumber + i;
                for (int j = 0; j < minRowSize; j++)
                    elementRow.cells.add(null);
                rows.add(elementRowId, elementRow);
                addedRowIds.add(elementRowId);
            }
            // Place all named entities
            for (int c = 0; c < serviceElements.length; c++) {
                final String[] entities = serviceElements[c];
                for (int r = 0; r < entities.length; r++)
                    rows.get(rowNumber + r).cells.set(cellIndexes[c], new Cell(entities[r], null));
            }
            // Advance to the next original row
            rowNumber += maxElements;
        }
    }
    
    /**
     * Delete rows that were added to contain extracted elements
     * @param project The project
     */
    protected void deleteRows(final Project project) {
        final List<Row> rows = project.rows;
        // Traverse rows IDs in reverse, from high to low,
        // to avoid index shifts as rows get deleted.
        for (int i = addedRowIds.size() - 1; i >= 0; i--) {
            final int addedRowId = addedRowIds.get(i);
            if (addedRowId >= rows.size())
                throw new IndexOutOfBoundsException(String.format("Needed to remove row %d, "
                                + "but only %d rows were available.", addedRowId, rows.size()));
            rows.remove(addedRowId);
        }
        addedRowIds.clear();
    }
    
    /**
     * Subclass of <tt>ColumnAdditionChange</tt>
     * that provides access to the cell index of the created column
     */
    protected static class CustomColumnAdditionChange extends ColumnAdditionChange {
        /**
         * Create a new <tt>CustomColumnAdditionChange</tt>
         * @param columnName The column name
         * @param columnIndex The column index
         * @param newCells The new cells
         */
        public CustomColumnAdditionChange(final String columnName, final int columnIndex,
                                          final List<CellAtRow> newCells) {
            super(columnName, columnIndex, newCells);
        }
        
        /**
         * Gets the cell index of the created column
         * @return The cell index
         */
        public int getCellIndex() {
            if (_newCellIndex < 0)
                throw new IllegalStateException("The cell index has not yet been set.");
            return _newCellIndex;
        }
    }
}
