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
