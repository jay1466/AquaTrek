import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button,
  TextField, Grid, CircularProgress, Box, Typography, Divider,
  IconButton, MenuItem, Select, FormControl, InputLabel, FormHelperText,
} from '@mui/material';
import { CloseOutlined } from '@mui/icons-material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import buildingApi from '@api/buildingApi';
import { getErrorMessage } from '@utils/helpers';
import { BUILDING_TYPES } from '@utils/constants';

const schema = z.object({
  name:         z.string().min(1, 'Building name is required.').max(255),
  code:         z.string().min(1, 'Code is required.').max(50)
                  .regex(/^[A-Za-z0-9\-_]+$/, 'Code: letters, digits, hyphens, underscores only.'),
  totalFloors:  z.coerce.number().min(1, 'Must have at least 1 floor.').max(200),
  totalUnits:   z.coerce.number().min(0).optional(),
  description:  z.string().max(1000).optional().or(z.literal('')),
  buildingType: z.string().optional(),
});

function BuildingFormModal({ open, onClose, building }) {
  const isEdit = Boolean(building);
  const qc = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  const { register, handleSubmit, reset, control, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { buildingType: 'RESIDENTIAL', totalFloors: 1, totalUnits: 0 },
  });

  useEffect(() => {
    if (open) reset(building || { buildingType: 'RESIDENTIAL', totalFloors: 1, totalUnits: 0 });
  }, [open, building, reset]);

  const mutation = useMutation({
    mutationFn: (data) => isEdit ? buildingApi.update(building.id, data) : buildingApi.create(data),
    onSuccess: () => {
      enqueueSnackbar(isEdit ? 'Building updated.' : 'Building created.', { variant: 'success' });
      qc.invalidateQueries({ queryKey: ['buildings'] });
      onClose();
    },
    onError: (err) => enqueueSnackbar(getErrorMessage(err), { variant: 'error' }),
  });

  const F = (name, label, opts = {}) => (
    <TextField {...register(name)} label={label} fullWidth size="small"
      error={!!errors[name]} helperText={errors[name]?.message} {...opts} />
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth
            PaperProps={{ sx: { borderRadius: '20px' } }}>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
        <Typography variant="h6" fontWeight={700}>
          {isEdit ? 'Edit Building' : 'Add Building'}
        </Typography>
        <IconButton size="small" onClick={onClose}><CloseOutlined fontSize="small" /></IconButton>
      </DialogTitle>
      <Divider />
      <DialogContent sx={{ pt: 3 }}>
        <Box component="form" id="bld-form" onSubmit={handleSubmit((d) => mutation.mutate(d))} noValidate>
          <Grid container spacing={2}>
            <Grid item xs={12} md={8}>{F('name', 'Building Name *')}</Grid>
            <Grid item xs={12} md={4}>{F('code', 'Code * (e.g. A)')}</Grid>
            <Grid item xs={6}>{F('totalFloors', 'Total Floors *', { type: 'number' })}</Grid>
            <Grid item xs={6}>{F('totalUnits', 'Total Units', { type: 'number' })}</Grid>
            <Grid item xs={12}>
              <Controller name="buildingType" control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.buildingType}>
                    <InputLabel>Building Type</InputLabel>
                    <Select {...field} label="Building Type">
                      {BUILDING_TYPES.map(o => (
                        <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>
                      ))}
                    </Select>
                    {errors.buildingType && <FormHelperText>{errors.buildingType.message}</FormHelperText>}
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12}>
              {F('description', 'Description (optional)', { multiline: true, rows: 2 })}
            </Grid>
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2, gap: 1 }}>
        <Button onClick={onClose} disabled={mutation.isPending} variant="outlined" sx={{ borderRadius: '10px' }}>
          Cancel
        </Button>
        <Button type="submit" form="bld-form" variant="contained" disabled={mutation.isPending}
                sx={{ borderRadius: '10px', minWidth: 120 }}
                startIcon={mutation.isPending ? <CircularProgress size={16} color="inherit" /> : null}>
          {mutation.isPending ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Building'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default BuildingFormModal;
