/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.deltahex;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.exbin.deltahex.CodeArea.NO_MODIFIER;
import org.exbin.deltahex.CodeArea.Section;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCommandHandler implements CodeAreaCommandHandler {

    private final CodeArea codeArea;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;

    public DefaultCommandHandler(CodeArea codeArea) {
        this.codeArea = codeArea;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (java.awt.HeadlessException ex) {
            // Create clipboard if system one not available
            clipboard = new Clipboard("test");
        }
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
            }
        });
        try {
            binaryDataFlavor = new DataFlavor("application/octet-stream");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
    }

    @Override
    public void caretMoved() {
        // Do nothing
    }

    @Override
    public void keyPressed(char keyValue) {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                BinaryData data = codeArea.getData();
                long dataPosition = codeArea.getDataPosition();
                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (dataPosition == codeArea.getData().getDataSize()) {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                } else {
                    // TODO code types
                    if (codeArea.getCodeOffset() > 0) {
                        byte lowerHalf = (byte) (data.getByte(dataPosition) & 0xf);
                        if (lowerHalf > 0) {
                            ((EditableBinaryData) data).insert(dataPosition + 1, 1);
                            ((EditableBinaryData) data).setByte(dataPosition + 1, lowerHalf);
                        }
                    } else {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                }
                codeArea.moveRight(CodeArea.NO_MODIFIER);
                codeArea.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && codeArea.isValidChar(keyValue)) {
                BinaryData data = codeArea.getData();
                CaretPosition caretPosition = codeArea.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = codeArea.charToBytes(keyChar);
                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (dataPosition < codeArea.getData().getDataSize()) {
                        int length = bytes.length;
                        if (dataPosition + length > codeArea.getData().getDataSize()) {
                            length = (int) (codeArea.getData().getDataSize() - dataPosition);
                        }
                        ((EditableBinaryData) data).remove(dataPosition, length);
                    }
                }
                ((EditableBinaryData) data).insert(dataPosition, bytes);
                codeArea.getCaret().setCaretPosition(dataPosition + bytes.length - 1);
                codeArea.moveRight(CodeArea.NO_MODIFIER);
                codeArea.revealCursor();
            }
        }
    }

    private void setCodeValue(int value) {
        CaretPosition caretPosition = codeArea.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        setCodeValue(dataPosition, value, caretPosition.getCodeOffset());
    }

    private void setCodeValue(long dataPosition, int value, int codeOffset) {
        BinaryData data = codeArea.getData();
        byte byteValue = data.getByte(dataPosition);

        // TODO other code types
        if (codeOffset == 1) {
            byteValue = (byte) ((byteValue & 0xf0) | value);
        } else {
            byteValue = (byte) ((byteValue & 0xf) | (value << 4));
        }

        ((EditableBinaryData) data).setByte(dataPosition, byteValue);
    }

    @Override
    public void backSpacePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
        } else {
            CodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getData().getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition - 1, 1);
                caret.setCodeOffset(0);
                codeArea.moveLeft(NO_MODIFIER);
                caret.setCodeOffset(0);
                codeArea.revealCursor();
                codeArea.computeDimensions();
                codeArea.updateScrollBars();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
        } else {
            CodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getData().getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition, 1);
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.computeDimensions();
                codeArea.updateScrollBars();
            }
        }
    }

    private void deleteSelection() {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getData()).remove(first, last - first + 1);
        codeArea.clearSelection();
        CodeAreaCaret caret = codeArea.getCaret();
        caret.setCaretPosition(first);
        codeArea.revealCursor();
        codeArea.computeDimensions();
        codeArea.updateScrollBars();
    }

    @Override
    public void delete() {
        if (!codeArea.isEditable()) {
            return;
        }

        deleteSelection();
    }

    @Override
    public void copy() {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            clipboard.setContents(binaryData, binaryData);
        }
    }

    @Override
    public void cut() {
        if (!codeArea.isEditable()) {
            return;
        }

        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            copy();
            deleteSelection();
        }
    }

    @Override
    public void paste() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
            if (codeArea.hasSelection()) {
                deleteSelection();
            }

            try {
                Object object = clipboard.getData(binaryDataFlavor);
                if (object instanceof BinaryData) {
                    CodeAreaCaret caret = codeArea.getCaret();
                    long dataPosition = caret.getDataPosition();

                    BinaryData data = (BinaryData) object;
                    long dataSize = data.getDataSize();
                    if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                        long toRemove = dataSize;
                        if (dataPosition + toRemove > codeArea.getData().getDataSize()) {
                            toRemove = codeArea.getData().getDataSize() - dataPosition;
                        }
                        ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                    }
                    ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), data);

                    caret.setCaretPosition(caret.getDataPosition() + dataSize);
                    caret.setCodeOffset(0);
                    codeArea.computeDimensions();
                    codeArea.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            if (codeArea.hasSelection()) {
                deleteSelection();
            }

            Object insertedData;
            try {
                insertedData = clipboard.getData(DataFlavor.stringFlavor);
                if (insertedData instanceof String) {
                    CodeAreaCaret caret = codeArea.getCaret();
                    long dataPosition = caret.getDataPosition();

                    byte[] bytes = ((String) insertedData).getBytes(Charset.forName("UTF-8"));
                    int length = bytes.length;
                    if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                        long toRemove = length;
                        if (dataPosition + toRemove > codeArea.getData().getDataSize()) {
                            toRemove = codeArea.getData().getDataSize() - dataPosition;
                        }
                        ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                    }
                    ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), bytes);

                    caret.setCaretPosition(caret.getDataPosition() + length);
                    caret.setCodeOffset(0);
                    codeArea.computeDimensions();
                    codeArea.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    public class BinaryDataClipboardData implements Transferable, ClipboardOwner {

        private final BinaryData data;

        public BinaryDataClipboardData(BinaryData data) {
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                data.saveToStream(byteArrayStream);
                return byteArrayStream.toString("UTF-8");
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }
    }
}
