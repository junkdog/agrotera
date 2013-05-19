package lombok.core.handlers;

import lombok.ast.IMethod;
import lombok.ast.IType;
import lombok.core.DiagnosticsReceiver;

public abstract class ArtemisSystemHandler<COMPILER_BINDING, TYPE_TYPE extends IType<METHOD_TYPE, ?, ?, ?, ?, ?>, METHOD_TYPE extends IMethod<TYPE_TYPE, ?, ?, ?>> extends AbstractArtemisHandler<COMPILER_BINDING,TYPE_TYPE,METHOD_TYPE>
{

	public ArtemisSystemHandler(DiagnosticsReceiver diagnosticsReceiver)
	{
		super(diagnosticsReceiver);
	}
}
