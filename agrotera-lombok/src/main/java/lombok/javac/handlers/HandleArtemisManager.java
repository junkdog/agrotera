package lombok.javac.handlers;


import java.util.List;

import org.kohsuke.MetaInfServices;

import lombok.ArtemisSystem;
import lombok.ast.Annotation;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.ast.JavacType;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

@MetaInfServices(JavacAnnotationHandler.class)
public class HandleArtemisManager extends JavacAnnotationHandler<ArtemisSystem>
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
		mappedComponentTypes.addAll(annotation.getActualExpressions("optional"));
		List<Object> systemTypes = annotation.getActualExpressions("systems");
		List<Object> managerTypes = annotation.getActualExpressions("managers");
		
		new JavacHandler(annotationNode)
			.handle(type, mappedComponentTypes, systemTypes, managerTypes);
	}
}
