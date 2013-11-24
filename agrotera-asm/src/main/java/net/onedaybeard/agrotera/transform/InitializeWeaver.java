package net.onedaybeard.agrotera.transform;

import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.MANAGER;
import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.POJO;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class InitializeWeaver extends MethodVisitor implements Opcodes
{
	private static final String WORLD = "com/artemis/World";
	private static final String WORLD_TYPE = "Lcom/artemis/World;";
	private static final String MAPPER_TYPE = "Lcom/artemis/ComponentMapper;";
	private static final String CLASS_OF_MAPPER_TYPE = "(Ljava/lang/Class;)" + MAPPER_TYPE;
	private static final String CLASS_OF_MANAGER_TYPE = "(Ljava/lang/Class;)Lcom/artemis/Manager;";
	private static final String CLASS_OF_SYSTEM_TYPE = "(Ljava/lang/Class;)Lcom/artemis/EntitySystem;";
	
	private String className;
	private ArtemisConfigurationData info;
	
	InitializeWeaver(MethodVisitor methodVisitor, String className, ArtemisConfigurationData info)
	{
		super(ASM4, methodVisitor);
		this.className = className;
		this.info = info;
	}

	@Override
	public void visitCode()
	{
		mv.visitCode();
		for (Type component : info.requires)
			injectMapper(component);
		for (Type component : info.requiresOne)
			injectMapper(component);
		for (Type component : info.optional)
			injectMapper(component);
		if (info.is(MANAGER)) for (Type component : info.exclude)
			injectMapper(component);
		for (Type manager : info.managers)
			injectWordly(manager, CLASS_OF_MANAGER_TYPE);
		for (Type system : info.systems)
			injectWordly(system, CLASS_OF_SYSTEM_TYPE);
	}
	
	private void injectMapper(Type component)
	{
		String mapperField = toLowerCamelCase(component) + "Mapper";
		
		beginInjectField(component);
		mv.visitMethodInsn(INVOKEVIRTUAL, WORLD, "getMapper", CLASS_OF_MAPPER_TYPE);
		mv.visitFieldInsn(PUTFIELD, className, mapperField, MAPPER_TYPE);
	}

	private void injectWordly(Type injectedType, String classType)
	{
		String field = toLowerCamelCase(injectedType);
		String method = getMethod(classType);
		
		beginInjectField(injectedType);
		mv.visitMethodInsn(INVOKEVIRTUAL, WORLD, method, classType);
		mv.visitTypeInsn(CHECKCAST, injectedType.getInternalName());
		mv.visitFieldInsn(PUTFIELD, className, field, injectedType.getDescriptor());
	}

	private void beginInjectField(Type injectedType)
	{
		mv.visitLabel(new Label());
		mv.visitVarInsn(ALOAD, 0);
		if (info.is(POJO)) {
			mv.visitVarInsn(ALOAD, 1);
		} else {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "world", WORLD_TYPE);
		}
		mv.visitLdcInsn(injectedType);
	}
	
	private static String getMethod(String classType)
	{
		if (classType.equals(CLASS_OF_SYSTEM_TYPE))
			return "getSystem";
		else if (classType.equals(CLASS_OF_MANAGER_TYPE))
			return "getManager";
		else
			throw new RuntimeException("Not a valid type: " + classType);
	}
	
	private static String toLowerCamelCase(Type type)
	{
		String name = type.getClassName();
		StringBuilder sb = new StringBuilder(
			name.substring(name.lastIndexOf(".") + 1, name.length()));
		
		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}
}
