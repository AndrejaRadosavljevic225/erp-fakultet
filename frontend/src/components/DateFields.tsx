import { DateInput, DateTimePicker, type DateInputProps, type DateTimePickerProps } from '@mantine/dates';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';

dayjs.extend(customParseFormat);

/**
 * Mantine podrazumevano parsira RUCNI unos samo kao "YYYY-MM-DD", pa bi "01.09.2026."
 * bilo protumaceno kao 9. januar. Zato eksplicitno parsiramo domace formate.
 */
const DATE_FORMATS = ['D.M.YYYY.', 'D.M.YYYY', 'DD.MM.YYYY.', 'DD.MM.YYYY', 'D/M/YYYY', 'YYYY-MM-DD'];

function parse(input: string, formats: string[], output: string): string | null {
  const trimmed = input.trim();
  if (!trimmed) return null;
  for (const format of formats) {
    const parsed = dayjs(trimmed, format, true);
    if (parsed.isValid()) return parsed.format(output);
  }
  return null;
}

/** Unos datuma sa domacim formatom (dd.mm.gggg.) i ispravnim parsiranjem rucnog unosa. */
export function SrDateInput(props: DateInputProps) {
  return (
    <DateInput
      valueFormat="DD.MM.YYYY."
      placeholder="dd.mm.gggg."
      clearable
      dateParser={(value) => parse(value, DATE_FORMATS, 'YYYY-MM-DD')}
      {...props}
    />
  );
}

/**
 * Izbor datuma i vremena; vrednost je string "YYYY-MM-DD HH:mm:ss".
 * DateTimePicker se popunjava iskljucivo iz kalendara (nema rucnog unosa),
 * pa mu parser nije potreban.
 */
export function SrDateTimePicker(props: DateTimePickerProps) {
  return (
    <DateTimePicker valueFormat="DD.MM.YYYY. HH:mm" placeholder="dd.mm.gggg. čč:mm" clearable {...props} />
  );
}
