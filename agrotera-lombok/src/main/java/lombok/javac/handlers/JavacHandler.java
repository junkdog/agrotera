package lombok.javac.handlers;

import static lombok.core.util.Names.decapitalize;
import static lombok.javac.handlers.ast.JavacResolver.CLASS;
import lombok.core.handlers.AbstractArtemisHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.ast.JavacMethod;
import lombok.javac.handlers.ast.JavacType;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;

class JavacHandler extends AbstractArtemisHandler<TypeSymbol,JavacType,JavacMethod>
{
	private JavacNode annotationNode;

	public JavacHandler(JavacType type, JavacNode diagnostic)
	{
		super(type);
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