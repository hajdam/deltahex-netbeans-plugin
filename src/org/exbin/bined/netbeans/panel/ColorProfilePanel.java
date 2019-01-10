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
package org.exbin.bined.netbeans.panel;

import org.exbin.framework.gui.utils.LanguageUtils;

/**
 * Color profile panel.
 *
 * @version 0.2.0 2019/01/10
 * @author ExBin Project (http://exbin.org)
 */
public class ColorProfilePanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ColorProfilePanel.class);

    public ColorProfilePanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        preferencesScrollPane = new javax.swing.JScrollPane();
        preferencesPanel = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout preferencesPanelLayout = new javax.swing.GroupLayout(preferencesPanel);
        preferencesPanel.setLayout(preferencesPanelLayout);
        preferencesPanelLayout.setHorizontalGroup(
            preferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 397, Short.MAX_VALUE)
        );
        preferencesPanelLayout.setVerticalGroup(
            preferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );

        preferencesScrollPane.setViewportView(preferencesPanel);

        add(preferencesScrollPane, java.awt.BorderLayout.WEST);

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 377, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 431, Short.MAX_VALUE)
        );

        add(previewPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel preferencesPanel;
    private javax.swing.JScrollPane preferencesScrollPane;
    private javax.swing.JPanel previewPanel;
    // End of variables declaration//GEN-END:variables
}