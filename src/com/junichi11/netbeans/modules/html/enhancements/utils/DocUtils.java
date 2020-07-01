/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package com.junichi11.netbeans.modules.html.enhancements.utils;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public final class DocUtils {

    private static final Logger LOGGER = Logger.getLogger(DocUtils.class.getName());

    private DocUtils() {
    }

    /**
     * Get img tag range.
     *
     * @param doc Document
     * @param offset caret position
     * @return range array if exists img tag, otherwise null
     * @throws BadLocationException
     */
    public static OffsetRange getImgRange(Document doc, int offset) throws BadLocationException {
        int start = offset;
        int end = offset;

        // start position
        while (start - 1 > 0) {
            if (doc.getText(start, offset - start).startsWith("<img")) { // NOI18N
                break;
            }
            String text = doc.getText(start - 1, 1);
            if (text.equals(">")) { // NOI18N
                return OffsetRange.NONE;
            }
            start--;
        }

        // end position
        int last = doc.getLength();
        while (end <= last) {
            if (doc.getText(offset, end - offset).endsWith("/>")) { // NOI18N
                break;
            }
            if (end == last) {
                return OffsetRange.NONE;
            }
            end++;
        }

        return new OffsetRange(start, end);
    }

    /**
     * Get img tag text
     *
     * @param doc Document
     * @param offset caret position
     * @return img tag text
     * @throws BadLocationException
     */
    @CheckForNull
    public static String getImgTag(Document doc, int offset) throws BadLocationException {
        OffsetRange range = getImgRange(doc, offset);
        if (range == OffsetRange.NONE) {
            return null;
        }

        return doc.getText(range.getStart(), range.getLength());
    }

    /**
     * Get FileObject from Document
     *
     * @param doc Document
     * @return FileObject
     */
    public static FileObject getFileObject(Document doc) {
        return GsfUtilities.findFileObject(doc);
    }

    /**
     * Convert to path for FileObject
     *
     * @param path
     * @return relative path
     */
    private static String normalizePath(@NonNull String path) {
        if (path.isEmpty()) {
            return path;
        }
        if (path.startsWith("./")) { // NOI18N
            path = "." + path; // NOI18N
        } else {
            path = "../" + path; // NOI18N
        }
        return path;
    }

    /**
     * Get Image
     *
     * @param path
     * @param doc
     * @return Image
     */
    @CheckForNull
    public static Image getImage(String path, Document doc) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // URL
        if (path.startsWith("http://") || path.startsWith("https://")) { // NOI18N
            try {
                return ImageIO.read(new URL(path));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

        // relative path
        path = normalizePath(path);
        FileObject current = getFileObject(doc);
        if (current == null) {
            return null;
        }
        FileObject target = current.getFileObject(path);
        if (target == null) {
            return null;
        }
        try {
            return ImageIO.read(FileUtil.toFile(target));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
}
