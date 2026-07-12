import React from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogContentText,
  DialogActions, Button, CircularProgress,
} from '@mui/material';
import { WarningAmberOutlined } from '@mui/icons-material';

/**
 * Reusable confirmation dialog for destructive actions.
 *
 * Usage:
 *   <ConfirmDialog
 *     open={open}
 *     title="Delete Building"
 *     message="Are you sure? This cannot be undone."
 *     onConfirm={handleDelete}
 *     onCancel={() => setOpen(false)}
 *     loading={isDeleting}
 *     confirmLabel="Delete"
 *     confirmColor="error"
 *   />
 */
function ConfirmDialog({
  open,
  title       = 'Confirm Action',
  message     = 'Are you sure you want to proceed?',
  onConfirm,
  onCancel,
  loading     = false,
  confirmLabel = 'Confirm',
  cancelLabel  = 'Cancel',
  confirmColor = 'error',
}) {
  return (
    <Dialog
      open={open}
      onClose={!loading ? onCancel : undefined}
      maxWidth="xs"
      fullWidth
      PaperProps={{ sx: { borderRadius: '16px' } }}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1, pt: 3 }}>
        <WarningAmberOutlined color="warning" />
        {title}
      </DialogTitle>

      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2.5, gap: 1 }}>
        <Button
          onClick={onCancel}
          disabled={loading}
          variant="outlined"
          sx={{ borderRadius: '10px', minWidth: 90 }}
        >
          {cancelLabel}
        </Button>
        <Button
          onClick={onConfirm}
          disabled={loading}
          variant="contained"
          color={confirmColor}
          sx={{ borderRadius: '10px', minWidth: 90 }}
          startIcon={loading ? <CircularProgress size={16} color="inherit" /> : undefined}
        >
          {loading ? 'Processing…' : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default ConfirmDialog;
