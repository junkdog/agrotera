/*
 * Copyright © 2010-2012 Philipp Eichhorn
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
package lombok.javac.handlers;

import static lombok.ast.AST.*;
import static lombok.core.util.ErrorMessages.*;
import static lombok.core.util.Names.decapitalize;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import static lombok.javac.handlers.ast.JavacResolver.CLASS;

import java.util.*;

import lombok.*;
import lombok.core.AnnotationValues;
import lombok.core.handlers.ArtemisConfigurationHandler;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
//import lombok.javac.ResolutionBased;
import lombok.javac.handlers.ast.JavacMethod;
import lombok.javac.handlers.ast.JavacType;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleArtemisConfiguration extends JavacAnnotationHandler<ArtemisConfiguration>
{

	@Override
	public void handle(final AnnotationValues<ArtemisConfiguration> annotation, final JCAnnotation source, final JavacNode annotationNode)
	{
		JavacType type = JavacType.typeOf(annotationNode, source);
		
		List<Object> mappedComponentTypes = annotation.getActualExpressions("requires");
		mappedComponentTypes.addAll(annotation.getActualExpressions("optional"));
		List<Object> systemTypes = annotation.getActualExpressions("systems");
		List<Object> managerTypes = annotation.getActualExpressions("managers");
		
		new Handler(annotationNode)
			.handle(type, mappedComponentTypes, systemTypes, managerTypes);
	}

	private static class Handler extends ArtemisConfigurationHandler<TypeSymbol,JavacType,JavacMethod>
	{
		private JavacNode annotationNode;

		public Handler(JavacNode diagnostic)
		{
			super(diagnostic, false);
			this.annotationNode = diagnostic;
		}

		@Override
		protected TypeSymbol getBinding(JavacType type, Object classLiteral)
		{
			JCFieldAccess literal = (JCFieldAccess)classLiteral;
			Type resolvedType = CLASS.resolveMember(annotationNode, literal.selected);
			return resolvedType.asElement();
		}

		@Override
		protected String toFieldName(TypeSymbol binding)
		{
			return decapitalize(binding.getSimpleName().toString());
		}

		@Override
		protected String toQualifiedName(TypeSymbol binding)
		{
			return binding.getQualifiedName().toString();
		}
	}
}
