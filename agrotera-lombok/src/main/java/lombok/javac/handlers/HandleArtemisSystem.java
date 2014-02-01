package lombok.javac.handlers;


import java.util.List;

import lombok.ast.pg.Annotation;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.ast.JavacType;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;

import org.kohsuke.MetaInfServices;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

@MetaInfServices(JavacAnnotationHandler.class)
public class HandleArtemisSystem extends JavacAnnotationHandler<ArtemisSystem>
{

	@Override
	public void handle(final AnnotationValues<ArtemisSystem> annotation, final JCAnnotation source, final JavacNode annotationNode)
	{
		JavacType type = JavacType.typeOf(annotationNode, source);
		for (Annotation a : type.annotations())
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
		
		if (mappedComponentTypes.size() == 0 
			&& annotation.getActualExpressions("excludes").size() > 0)
		{
			annotationNode.addError(
				"Excludes is only possible with at least 'requires' or 'requiresOne'");
			
			return;
		}
		
		new JavacHandler(type, annotationNode)
				.handle(mappedComponentTypes, systemTypes, managerTypes)
				.rebuild();
	}
}
