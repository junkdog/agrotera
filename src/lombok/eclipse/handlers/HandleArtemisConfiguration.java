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
package lombok.eclipse.handlers;

import static lombok.ast.AST.*;
import static lombok.core.util.Names.*;
import static lombok.core.util.ErrorMessages.*;
import static lombok.eclipse.handlers.Eclipse.ensureAllClassScopeMethodWereBuild;

import java.util.*;

import lombok.*;
import lombok.core.AnnotationValues;
import lombok.core.DiagnosticsReceiver;
import lombok.core.handlers.ArtemisConfigurationHandler;
import lombok.eclipse.DeferUntilBuildFieldsAndMethods;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseMethod;
import lombok.eclipse.handlers.ast.EclipseType;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@link ListenerSupport} annotation for eclipse using the {@link PatchListenerSupport}.
 */
@ProviderFor(EclipseAnnotationHandler.class)
@DeferUntilBuildFieldsAndMethods
//@DeferUntilPostDiet
public class HandleArtemisConfiguration extends EclipseAnnotationHandler<ArtemisConfiguration>
{
	@Override
	public void handle(final AnnotationValues<ArtemisConfiguration> annotation, final Annotation source, final EclipseNode annotationNode) {
		EclipseType type = EclipseType.typeOf(annotationNode, source);
		if (type.isAnnotation() || type.isInterface()) {
			annotationNode.addError(canBeUsedOnClassOnly(ArtemisConfiguration.class));
			return;
		}

		List<Object> mappedComponentTypes = annotation.getActualExpressions("requires");
		mappedComponentTypes.addAll(annotation.getActualExpressions("optional"));
		List<Object> systemTypes = annotation.getActualExpressions("systems");
		List<Object> managerTypes = annotation.getActualExpressions("managers");
		
		filterInvalid(mappedComponentTypes);
		filterInvalid(systemTypes);
		filterInvalid(managerTypes);
		
		new Handler(annotationNode)
			.handle(type, mappedComponentTypes, systemTypes, managerTypes);
	}
	
	private static void filterInvalid(List<Object> types)
	{
		for (Iterator<Object> it = types.iterator(); it.hasNext();)
		{
			if (!(it.next() instanceof ClassLiteralAccess))
				it.remove();
		}
	}
	
	private static class Handler extends ArtemisConfigurationHandler<TypeBinding,EclipseType,EclipseMethod>
	{
		public Handler(DiagnosticsReceiver diagnostic)
		{
			super(diagnostic, true);
		}

		@Override
		protected TypeBinding getBinding(EclipseType type, Object classLiteral)
		{
			TypeReference componentRef = ((ClassLiteralAccess)classLiteral).type;
			return componentRef.resolveType(type.get().initializerScope);
		}

		@Override
		protected String toFieldName(TypeBinding binding)
		{
			return decapitalize(String.valueOf(binding.sourceName()));
		}

		@Override
		protected String toQualifiedName(TypeBinding binding)
		{
			return new StringBuilder()
				.append(binding.qualifiedPackageName())
				.append('.')
				.append(binding.sourceName()).toString();
		}
	}
}
