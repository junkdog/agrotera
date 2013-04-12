/*
 * Copyright © 2012 Philipp Eichhorn
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
package lombok.ast;

import java.util.List;

public interface ITypeEditor<METHOD_TYPE extends IMethod<?, ?, ?, ?>, AST_BASE_TYPE, AST_TYPE_DECL_TYPE, AST_METHOD_DECL_TYPE> {
	public <T extends AST_BASE_TYPE> T build(Node<?> node);

	public <T extends AST_BASE_TYPE> T build(Node<?> node, Class<T> extectedType);

	public <T extends AST_BASE_TYPE> List<T> build(List<? extends Node<?>> nodes);

	public <T extends AST_BASE_TYPE> List<T> build(List<? extends Node<?>> nodes, Class<T> extectedType);

	public void injectInitializer(Initializer initializerBlock);

	public void injectField(FieldDecl fieldDecl);

	public void injectField(EnumConstant enumConstant);

	public AST_METHOD_DECL_TYPE injectMethod(MethodDecl methodDecl);

	public AST_METHOD_DECL_TYPE injectConstructor(ConstructorDecl constructorDecl);

	public void injectType(ClassDecl typeDecl);

	public void removeMethod(METHOD_TYPE method);

	public void makeEnum();

	public void makePrivate();

	public void makePackagePrivate();

	public void makeProtected();

	public void makePublic();

	public void makeStatic();

	public void rebuild();
}
