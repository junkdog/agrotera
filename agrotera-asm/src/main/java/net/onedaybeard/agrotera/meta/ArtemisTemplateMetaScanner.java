package net.onedaybeard.agrotera.meta;

import static net.onedaybeard.agrotera.ProcessArtemis.WOVEN_ANNOTATION;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Scans a given file for everything tagged with {@link ArtemisTemplate}
 * 
 * @author GJ Roelofs info@codepoke.net
 * 
 */
public class ArtemisTemplateMetaScanner
		extends ClassVisitor {

	static final String TEMPLATE_ANNOTATION = "Llombok/ArtemisTemplate;";
	static final String FIELD_COMPONENTS = "components";
	static final String FIELD_NAME = "name";
	static final String FIELD_DESCRIPTION = "description";

	private List<ArtemisTemplateConfigurationData> list;
	private boolean skip = false;

	public ArtemisTemplateMetaScanner(List<ArtemisTemplateConfigurationData> list) {
		super(Opcodes.ASM4);
		this.list = list;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, final String nameField, final String descField, String signature, final Object value) {
		if (skip)
			return null; // Skip everything

		return new FieldVisitor(Opcodes.ASM4) {

			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

				if (desc.equals(TEMPLATE_ANNOTATION)) {

					// We found a class with a Template Annotation
					System.err.println(String.format("Found annotation %s on field %s :: %s :: %s", desc, nameField, descField, value));

					// The visitor that will construct the correct ArtemisTemplateConfigurationData
					return new AnnotationVisitor(Opcodes.ASM4) {

						ArtemisTemplateConfigurationData template = new ArtemisTemplateConfigurationData();
						boolean exploringComponents = false;

						@Override
						public void visit(String name, Object value) {
							System.err.println(String.format("Found value %s :: %s", name, value));

							if (exploringComponents) {
								template.components.add((Type) value);
							} else if (FIELD_DESCRIPTION.equals(name)) {
								template.description = (String) value;
							} else if (FIELD_NAME.equals(name)) {
								template.name = (String) value;
							}
						}

						@Override
						public AnnotationVisitor visitArray(String name) {
							System.err.println(String.format("Found array %s", name));
							if (FIELD_COMPONENTS.equals(name)) {
								exploringComponents = true;
								// Ensure that this also gets to access the values of the array, components
								return this;
							}

							return null;
						}

						public void visitEnd() {
							// add the result to the list if we are done at the top level
							if (!exploringComponents)
								list.add(template);

							exploringComponents = false;
						}
					};
				} else {
					return super.visitAnnotation(desc, visible);
				}
			}
		};
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (WOVEN_ANNOTATION.equals(desc)) {
			skip = true;
			return super.visitAnnotation(desc, visible);
		}

		return super.visitAnnotation(desc, visible);
	}

}
