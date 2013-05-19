package net.onedaybeard.agrotera.transform;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class ManagerMethodWeaver extends MethodVisitor implements Opcodes
{
	private static final String ENTITY = "com/artemis/Entity";
	private static final String ENTITY_TYPE = "(Lcom/artemis/Entity;)Z";
	private static final String MAPPER_TYPE = "Lcom/artemis/ComponentMapper;";
	
	private String className;
	private ArtemisConfigurationData info;
	
	ManagerMethodWeaver(MethodVisitor methodVisitor, String className, ArtemisConfigurationData info)
	{
		super(ASM4, methodVisitor);
		this.className = className;
		this.info = info;
	}

	@Override
	public void visitCode()
	{
		Label exitLabel = new Label();
		mv.visitCode();
		mv.visitFrame(F_NEW, 2, new Object[] {className, ENTITY}, 0, new Object[]{});
		
		for (Type component : info.requires)
			injectIfCheck(component, IFNE, exitLabel);
		for (Type component : info.exclude)
			injectIfCheck(component, IFEQ, exitLabel);
		
		mv.visitInsn(RETURN);
		mv.visitLabel(exitLabel);
		mv.visitFrame(F_NEW, 2, new Object[] {className, ENTITY}, 0, new Object[]{});
	}
	
	private void injectIfCheck(Type component, int jumpInstruction, Label jumpLabel)
	{
		String mapperField = toLowerCamelCase(component) + "Mapper";
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, mapperField, MAPPER_TYPE);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/artemis/ComponentMapper", "has", ENTITY_TYPE);
		mv.visitJumpInsn(jumpInstruction, jumpLabel);
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
