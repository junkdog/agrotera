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
