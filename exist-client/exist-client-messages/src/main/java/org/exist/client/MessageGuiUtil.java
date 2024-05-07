/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.client;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Simple Message GUI utils.
 */
public class MessageGuiUtil {

  /**
   * Show an error dialog.
   *
   * @param message the error message.
   * @param t the exception, or null.
   */
  public static void showErrorMessage(final String message, @Nullable final Throwable t) {
    JScrollPane scroll = null;
    final JTextArea msgArea = new JTextArea(message);
    msgArea.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClientFrame.214"))); //$NON-NLS-1$
    msgArea.setEditable(false);
    msgArea.setBackground(null);
    if (t != null) {
      try (final StringWriter out = new StringWriter();
           final PrintWriter writer = new PrintWriter(out)) {
        t.printStackTrace(writer);
        final JTextArea stacktrace = new JTextArea(out.toString(), 20, 50);
        stacktrace.setBackground(null);
        stacktrace.setEditable(false);
        scroll = new JScrollPane(stacktrace);
        scroll.setPreferredSize(new Dimension(250, 300));
        scroll.setBorder(BorderFactory
            .createTitledBorder(Messages.getString("ClientFrame.215"))); //$NON-NLS-1$
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      }
    }
    final JOptionPane optionPane = new JOptionPane();
    optionPane.setMessage(new Object[]{msgArea, scroll});
    optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
    final JDialog dialog = optionPane.createDialog(null, Messages.getString("ClientFrame.216")); //$NON-NLS-1$
    dialog.setResizable(true);
    dialog.pack();
    dialog.setVisible(true);
  }

  /**
   * Show an error query dialog.
   *
   * @param message the error message.
   * @param t the exception, or null.
   *
   * @return the result of the query.
   */
  public static int showErrorMessageQuery(final String message, @Nullable final Throwable t) {
    final JTextArea msgArea = new JTextArea(message);
    msgArea.setLineWrap(true);
    msgArea.setWrapStyleWord(true);
    msgArea.setEditable(false);
    msgArea.setBackground(null);
    JScrollPane scrollMsgArea = new JScrollPane(msgArea);
    scrollMsgArea.setPreferredSize(new Dimension(600, 300));
    scrollMsgArea.setBorder(BorderFactory
        .createTitledBorder(Messages.getString("ClientFrame.217"))); //$NON-NLS-1$

    JScrollPane scrollStacktrace = null;
    if (t != null) {
      try (final StringWriter out = new StringWriter();
           final PrintWriter writer = new PrintWriter(out)) {
        t.printStackTrace(writer);
        final JTextArea stacktrace = new JTextArea(out.toString(), 20, 50);
        stacktrace.setLineWrap(true);
        stacktrace.setWrapStyleWord(true);
        stacktrace.setBackground(null);
        stacktrace.setEditable(false);
        scrollStacktrace = new JScrollPane(stacktrace);
        scrollStacktrace.setPreferredSize(new Dimension(600, 300));
        scrollStacktrace.setBorder(BorderFactory
            .createTitledBorder(Messages.getString("ClientFrame.218"))); //$NON-NLS-1$
      } catch (final IOException ioe) {
        ioe.printStackTrace();
      }
    }

    final JOptionPane optionPane = new JOptionPane();
    optionPane.setMessage(new Object[]{scrollMsgArea, scrollStacktrace});
    optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
    optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(null, Messages.getString("ClientFrame.219")); //$NON-NLS-1$
    dialog.setResizable(true);
    dialog.pack();
    dialog.setVisible(true);

    final Object result = optionPane.getValue();
    if (result == null) {
      return 2;
    }
    return (Integer) optionPane.getValue();
  }
}
