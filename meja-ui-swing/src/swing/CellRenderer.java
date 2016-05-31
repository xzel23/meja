/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package swing;

import com.dua3.meja.model.Cell;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Interface for cell renderers.
 */
public interface CellRenderer {

    /**
     * Render cell content.
     * @param g the {@link Graphics2D} to use for rendering
     * @param cell the cell whose content shall be rendered
     * @param cellRect the rectangle taken up by the cell
     * @param clipRect the clipping rectangle
     * @param scale the scla to apply when rendering
     */
    void render(Graphics2D g, Cell cell, Rectangle cellRect, Rectangle clipRect, float scale);

}