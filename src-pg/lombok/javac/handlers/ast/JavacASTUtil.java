/*
 * Copyright © 2011-2012 Philipp Eichhorn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers.ast;

import static lombok.ast.AST.*;
import static lombok.core.util.Names.*;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;

import lombok.*;
import lombok.core.util.Is;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JavacASTUtil {

	public static lombok.ast.TypeRef boxedType(final JCExpression type) {
		if (type == null) return null;
		lombok.ast.TypeRef boxedType = Type(type);
		if (type instanceof JCPrimitiveTypeTree) {
			final String name = type.toString();
			if ("int".equals(name)) {
				boxedType = Type(Integer.class);
			} else if ("char".equals(name)) {
				boxedType = Type(Character.class);
			} else if (Is.oneOf(name, "void", "boolean", "float", "double", "byte", "short", "long")) {
				boxedType = Type("java.lang." + capitalize(name));
			}
		}
		return boxedType;
	}
}
