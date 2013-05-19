package lombok.eclipse.handlers;

import static lombok.core.util.ErrorMessages.canBeUsedOnClassOnly;

import java.util.Iterator;
import java.util.List;

import lombok.ArtemisManager;
import lombok.ArtemisSystem;
import lombok.ListenerSupport;
import lombok.core.AnnotationValues;
import lombok.eclipse.DeferUntilBuildFieldsAndMethods;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseType;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.kohsuke.MetaInfServices;

/**
 * Handles the {@link ListenerSupport} annotation for eclipse using the {@link PatchListenerSupport}.
 */
@DeferUntilBuildFieldsAndMethods
@MetaInfServices(EclipseAnnotationHandler.class)
//@DeferUntilPostDiet
public class HandleArtemisManager extends EclipseAnnotationHandler<ArtemisManager>
{
	@Override
	public void handle(final AnnotationValues<ArtemisManager> annotation, final Annotation source, final EclipseNode annotationNode) {
		EclipseType type = EclipseType.typeOf(annotationNode, source);
		if (type.isAnnotation() || type.isInterface()) {
			annotationNode.addError(canBeUsedOnClassOnly(ArtemisManager.class));
			return;
		}
		
		for (lombok.ast.Annotation a : type.annotations())
		{
			// because all else is null... 
			if (a.toString().startsWith("@WovenByTheHuntress"))
				return;
		}
		
		List<Object> mappedComponentTypes = annotation.getActualExpressions("requires");
		mappedComponentTypes.addAll(annotation.getActualExpressions("optional"));
		List<Object> systemTypes = annotation.getActualExpressions("systems");
		List<Object> managerTypes = annotation.getActualExpressions("managers");
		
		filterInvalid(mappedComponentTypes);
		filterInvalid(systemTypes);
		filterInvalid(managerTypes);
		
		new EclipseHandler(annotationNode)
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
}
