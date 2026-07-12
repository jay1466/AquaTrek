import React from 'react';
import { Chip } from '@mui/material';
import { STATUS_COLORS, STATUS_LABELS } from '@utils/constants';

/**
 * Reusable status badge chip.
 * Maps status strings to MUI colour variants.
 */
function StatusChip({ status, size = 'small' }) {
  const color = STATUS_COLORS[status] || 'default';
  const label = STATUS_LABELS[status] || status;
  return (
    <Chip
      label={label}
      color={color}
      size={size}
      variant="filled"
      sx={{ fontWeight: 600, fontSize: 11, borderRadius: '6px' }}
    />
  );
}

export default StatusChip;
