package lombok.core.handlers;

import static lombok.ast.AST.Assign;
import static lombok.ast.AST.Call;
import static lombok.ast.AST.Field;
import static lombok.ast.AST.FieldDecl;
import static lombok.ast.AST.Type;
import static lombok.core.util.Names.decapitalize;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.ast.Assignment;
import lombok.ast.FieldDecl;
import lombok.ast.IMethod;
import lombok.ast.IType;
import lombok.ast.StringLiteral;
import lombok.core.DiagnosticsReceiver;

@RequiredArgsConstructor
public abstract class ArtemisSystemHandler<COMPILER_BINDING, TYPE_TYPE extends IType<METHOD_TYPE, ?, ?, ?, ?, ?>, METHOD_TYPE extends IMethod<TYPE_TYPE, ?, ?, ?>>
{
	@SuppressWarnings("unused")
	private final DiagnosticsReceiver diagnosticsReceiver;

	public void handle(TYPE_TYPE type, List<Object> mappedComponentTypes,
		List<Object> systemTypes, List<Object> managerTypes)
	{
		for (Object component : mappedComponentTypes)
		{
			type.editor().injectField(createMapperField(getBinding(type, component)));
		}
		
		for (Object system : systemTypes)
		{
			type.editor().injectField(createField(getBinding(type, system)));
		}
		
		for (Object manager : managerTypes)
		{
			type.editor().injectField(createField(getBinding(type, manager)));
		}
		
		type.editor().rebuild();
	}
	
	protected Assignment assignField(String fieldName, String invokeMethod,
		StringLiteral qualifiedArgumentType)
	{
		return Assign(Field(fieldName),
			Call(Field("world"), invokeMethod).withArgument(qualifiedArgumentType));
	}

	private FieldDecl createField(COMPILER_BINDING type)
	{
		String name = toFieldName(type);
		
		return FieldDecl(Type(toQualifiedName(type)), name)
			.makePrivate();
	}
	
	private FieldDecl createMapperField(COMPILER_BINDING componentType)
	{
		String name = decapitalize(toFieldName(componentType)) + "Mapper";
		
		return FieldDecl(Type("com.artemis.ComponentMapper")
				.withTypeArgument(Type(toQualifiedName(componentType))),
				name)
			.makePrivate();
	}
	
	protected abstract COMPILER_BINDING getBinding(TYPE_TYPE type, Object classLiteral);
	protected abstract String toFieldName(COMPILER_BINDING binding);
	protected abstract String toQualifiedName(COMPILER_BINDING binding);
}
