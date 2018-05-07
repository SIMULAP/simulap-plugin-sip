//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.sampler.gui;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class DynamicVerticalPanel extends JPanel {

	private static final long serialVersionUID = 583890349816531548L;

	private Box subPanel = Box.createVerticalBox();

    private float horizontalAlign;

    private int vgap;

    public DynamicVerticalPanel() {
        this(5, LEFT_ALIGNMENT);
    }

    public DynamicVerticalPanel(Color bkg) {
        this();
        subPanel.setBackground(bkg);
        this.setBackground(bkg);
    }

    public DynamicVerticalPanel(int vgap, float horizontalAlign) {
        super(new BorderLayout());
        add(subPanel, BorderLayout.NORTH);
        this.vgap = vgap;
        this.horizontalAlign = horizontalAlign;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component c) {
        // This won't work right if we remove components. But we don't, so I'm
        // not going to worry about it right now.
        if (vgap > 0 && subPanel.getComponentCount() > 0) {
            subPanel.add(Box.createVerticalStrut(vgap));
        }

        if (c instanceof JComponent) {
            ((JComponent) c).setAlignmentX(horizontalAlign);
        }

        return subPanel.add(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Container#add(java.awt.Component)
     */
    public void remove(Component c) {
        subPanel.remove(c);
    }
}
