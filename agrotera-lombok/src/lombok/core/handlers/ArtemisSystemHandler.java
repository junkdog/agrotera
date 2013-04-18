package lombok.core.handlers;

import static lombok.ast.AST.*;
import static lombok.core.util.ErrorMessages.*;
import static lombok.core.util.Names.decapitalize;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.ast.*;
import lombok.core.DiagnosticsReceiver;

@RequiredArgsConstructor
public abstract class ArtemisSystemHandler<COMPILER_BINDING, TYPE_TYPE extends IType<METHOD_TYPE, ?, ?, ?, ?, ?>, METHOD_TYPE extends IMethod<TYPE_TYPE, ?, ?, ?>>
{
	private static final String INITIALIZE_METHOD = "initialize";
	
	private final DiagnosticsReceiver diagnosticsReceiver;
	private final boolean forceDummyInitializer;

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
		
//		hookUpInitializeMethod(type, mappedComponentTypes, systemTypes, managerTypes);
		type.editor().rebuild();
	}
	
	private void hookUpInitializeMethod(TYPE_TYPE type, List<Object> mappedComponentTypes, List<Object> systemTypes,
		List<Object> managerTypes)
	{
		List<Statement<?>> statements = new ArrayList<Statement<?>>();
		statements.addAll(assignMappers(type, mappedComponentTypes));
		statements.addAll(assignSystems(type, systemTypes));
		statements.addAll(assignManagers(type, managerTypes));

		boolean initializeMethodExists = type.hasMethod(INITIALIZE_METHOD); 
		if (initializeMethodExists && !forceDummyInitializer)
			prependInitializeBody(type, statements);
		else if (initializeMethodExists && forceDummyInitializer)
			rewriteInitializeSignatureWarning(type, statements);
		
		if (!initializeMethodExists || forceDummyInitializer)
		{
			MethodDecl method = MethodDecl(Type("void"), INITIALIZE_METHOD).makeProtected()
				.withAnnotation(Annotation(Type(Override.class)));
			method.withStatements(statements);
			type.editor().injectMethod(method);
		}
	}
	
	
	private void rewriteInitializeSignatureWarning(TYPE_TYPE type, List<Statement<?>> statements)
	{
		diagnosticsReceiver.addWarning("Can't prepend to initialize() with EJC yet...");
		type.editor().removeMethod(getMethod(type, INITIALIZE_METHOD, false));
	}

	private void rewriteInitializeSignature(TYPE_TYPE type, List<Statement<?>> statements)
	{
		METHOD_TYPE method = getMethod(type, INITIALIZE_METHOD, false);
		
		method.editor().replaceArguments(Arg(Type("java.lang.Object"), "dummy"));
		method.editor().makePublic();
		
		method.editor().rebuild();
		type.editor().rebuild();
		
		statements.add(Call(This(), INITIALIZE_METHOD).withArgument(Null()));
	}
	
	private METHOD_TYPE getMethod(TYPE_TYPE owner, String name, boolean hasArguments)
	{
		for (METHOD_TYPE method : owner.methods())
		{
			if (method.name().equals(name) && method.hasArguments() == hasArguments)
				return method;
		}
		System.out.println("method not found: " + name);
		return null;
	}
	
	private void prependInitializeBody(TYPE_TYPE type, List<Statement<?>> prepended)
	{
		METHOD_TYPE method = getMethod(type, INITIALIZE_METHOD, false);
		List<Statement<?>> existingBody = new ArrayList<Statement<?>>();
		existingBody.addAll(prepended);
		if (!forceDummyInitializer)
			existingBody.addAll(method.statements());
		method.editor().replaceBody(existingBody);
		method.editor().rebuild();
	}

	private List<Statement<?>> assignSystems(TYPE_TYPE type, List<Object> systemTypes)
	{
		List<Statement<?>> statements = new ArrayList<Statement<?>>();
		for (Object system : systemTypes)
		{
			COMPILER_BINDING binding = getBinding(type, system);
			String fieldName = toFieldName(binding);
			StringLiteral qualifiedName = String(toQualifiedName(binding));
			
			statements.add(assignField(fieldName, "getSystem", qualifiedName));
		}
		return statements;
	}
	
	private List<Statement<?>> assignManagers(TYPE_TYPE type, List<Object> managerTypes)
	{
		List<Statement<?>> statements = new ArrayList<Statement<?>>();
		for (Object system : managerTypes)
		{
			COMPILER_BINDING binding = getBinding(type, system);
			String fieldName = toFieldName(binding);
			StringLiteral qualifiedName = String(toQualifiedName(binding));
			
			statements.add(assignField(fieldName, "getManager", qualifiedName));
		}
		return statements;
	}

	private List<Statement<?>> assignMappers(TYPE_TYPE type, List<Object> mappedComponentTypes)
	{
		List<Statement<?>> statements = new ArrayList<Statement<?>>();
		for (Object component : mappedComponentTypes)
		{
			COMPILER_BINDING binding = getBinding(type, component);
			String fieldName = toFieldName(binding) +  "Mapper";
			StringLiteral qualifiedName = String(toQualifiedName(binding));
			
			statements.add(assignField(fieldName, "getMapper", qualifiedName));
		}
		return statements;
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
