package name.abuchen.portfolio.ui.util;

import name.abuchen.portfolio.model.Values;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ValueEditingSupport extends PropertyEditingSupport
{
    private final StringToCurrencyConverter stringToLong;
    private final CurrencyToStringConverter longToString;

    public ValueEditingSupport(Class<?> subjectType, String attributeName, Values<? extends Number> valueType)
    {
        super(subjectType, attributeName);

        Class<?> propertyType = descriptor().getPropertyType();
        if (!long.class.isAssignableFrom(propertyType) && !int.class.isAssignableFrom(propertyType))
            throw new UnsupportedOperationException(String.format(
                            "Property %s needs to be of type long or int to serve as decimal", attributeName)); //$NON-NLS-1$

        this.stringToLong = new StringToCurrencyConverter(valueType);
        this.longToString = new CurrencyToStringConverter(valueType);
    }

    @Override
    public CellEditor createEditor(Composite composite)
    {
        TextCellEditor textEditor = new TextCellEditor(composite);
        ((Text) textEditor.getControl()).setTextLimit(20);
        ((Text) textEditor.getControl()).addVerifyListener(new NumberVerifyListener());
        return textEditor;
    }

    @Override
    public final Object getValue(Object element) throws Exception
    {
        return longToString.convert(descriptor().getReadMethod().invoke(adapt(element)));
    }

    @Override
    public void setValue(Object element, Object value) throws Exception
    {
        Object subject = adapt(element);

        Number newValue = (Number) stringToLong.convert(String.valueOf(value));
        if (int.class.isAssignableFrom(descriptor().getPropertyType())
                        || Integer.class.isAssignableFrom(descriptor().getPropertyType()))
            newValue = Integer.valueOf(newValue.intValue());

        Number oldValue = (Number) descriptor().getReadMethod().invoke(subject);

        if (!newValue.equals(oldValue))
        {
            descriptor().getWriteMethod().invoke(subject, newValue);
            notify(element, newValue, oldValue);
        }
    }
}
