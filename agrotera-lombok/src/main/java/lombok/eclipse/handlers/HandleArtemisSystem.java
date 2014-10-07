package lombok.eclipse.handlers;

import static lombok.core.util.ErrorMessages.canBeUsedOnClassOnly;
import static lombok.eclipse.handlers.EclipseUtil.filterInvalid;

import java.util.List;

import lombok.ListenerSupport;
import lombok.core.AnnotationValues;
import lombok.eclipse.DeferUntilBuildFieldsAndMethods;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseType;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.kohsuke.MetaInfServices;

/**
 * Handles the {@link ListenerSupport} annotation for eclipse using the {@link PatchListenerSupport}.
 */
@DeferUntilBuildFieldsAndMethods
@MetaInfServices(EclipseAnnotationHandler.class)
public class HandleArtemisSystem extends EclipseAnnotationHandler<ArtemisSystem>
{
	@Override
	public void handle(final AnnotationValues<ArtemisSystem> annotation, final Annotation source, final EclipseNode annotationNode) {
		EclipseType type = EclipseType.typeOf(annotationNode, source);
		if (type.isAnnotation() || type.isInterface()) {
			annotationNode.addError(canBeUsedOnClassOnly(ArtemisSystem.class));
			return;
		}
		
		for (lombok.ast.pg.Annotation a : type.annotations())
		{
			// because all else is null... 
			if (a.toString().startsWith("@WovenByTheHuntress"))
				return;
		}
		
		List<Object> mappedComponentTypes = annotation.getActualExpressions("requires");
		mappedComponentTypes.addAll(annotation.getActualExpressions("requiresOne"));
		mappedComponentTypes.addAll(annotation.getActualExpressions("optional"));
		List<Object> systemTypes = annotation.getActualExpressions("systems");
		List<Object> managerTypes = annotation.getActualExpressions("managers");
		
		filterInvalid(mappedComponentTypes);
		filterInvalid(systemTypes);
		filterInvalid(managerTypes);
		
		if (mappedComponentTypes.size() == 0 
			&& annotation.getActualExpressions("excludes").size() > 0)
		{
			annotationNode.addError(
				"Excludes is only possible with at least 'requires' or 'requiresOne'");
			
			return;
		}
		
		new EclipseHandler(type)
				.handle(mappedComponentTypes, systemTypes, managerTypes)
				.rebuild();
	}
}
