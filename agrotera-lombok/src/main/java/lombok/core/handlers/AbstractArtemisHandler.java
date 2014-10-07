package lombok.core.handlers;

import static lombok.ast.pg.AST.*;
import static lombok.core.util.Names.decapitalize;

import java.util.List;

import lombok.ast.pg.FieldDecl;
import lombok.ast.pg.IMethod;
import lombok.ast.pg.IType;

public abstract class AbstractArtemisHandler<COMPILER_BINDING, TYPE_TYPE extends IType<METHOD_TYPE,?,?,?,?,?>, METHOD_TYPE extends IMethod<TYPE_TYPE,?,?,?>>
{
	private final TYPE_TYPE type;
	
	public AbstractArtemisHandler(TYPE_TYPE type)
	{
		this.type = type;
	}
	
	public AbstractArtemisHandler<COMPILER_BINDING,TYPE_TYPE,METHOD_TYPE> handle(List<Object> mappedComponentTypes, List<Object> systemTypes, List<Object> managerTypes)
	{
		for (Object component : mappedComponentTypes)
			type.editor().injectField(createMapperField(getBinding(type, component)));
		
		for (Object system : systemTypes)
			type.editor().injectField(createField(getBinding(type, system)));
		
		for (Object manager : managerTypes)
			type.editor().injectField(createField(getBinding(type, manager)));
		
		return this;
	}

	public AbstractArtemisHandler<COMPILER_BINDING,TYPE_TYPE,METHOD_TYPE> rebuild()
	{
		type.editor().rebuild();
		
		return this;
	}

	public AbstractArtemisHandler<COMPILER_BINDING,TYPE_TYPE,METHOD_TYPE> injectInitialize()
	{
		type.editor().injectMethod(MethodDecl(Type(void.class), "initialize")
				.withArgument(Arg(Type("com.artemis.World"), "world"))
				.makePublic());
		
		return this;
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
				.withTypeArgument(Type(toQualifiedName(componentType))), name)
				.withAnnotation(Annotation(Type(SuppressWarnings.class))
					.withValue(String("all"))) // not sure why this bleeds...
				.makePrivate();
	}

	protected abstract COMPILER_BINDING getBinding(TYPE_TYPE type,Object classLiteral);
	protected abstract String toFieldName(COMPILER_BINDING binding);
	protected abstract String toQualifiedName(COMPILER_BINDING binding);
}
