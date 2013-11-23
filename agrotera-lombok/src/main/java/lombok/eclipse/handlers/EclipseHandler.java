package lombok.eclipse.handlers;

import static lombok.core.util.Names.decapitalize;
import lombok.core.handlers.AbstractArtemisHandler;
import lombok.eclipse.handlers.ast.EclipseMethod;
import lombok.eclipse.handlers.ast.EclipseType;

import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

class EclipseHandler extends AbstractArtemisHandler<TypeBinding,EclipseType,EclipseMethod>
{
	public EclipseHandler(EclipseType type)
	{
		super(type);
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