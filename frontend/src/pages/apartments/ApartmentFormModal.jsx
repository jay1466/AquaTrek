import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, TextField, Grid, CircularProgress, Box, Typography,
  Divider, IconButton,
} from '@mui/material';
import { CloseOutlined } from '@mui/icons-material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import apartmentApi from '@api/apartmentApi';
import { getErrorMessage } from '@utils/helpers';

const schema = z.object({
  name:               z.string().min(3, 'Name must be at least 3 characters.').max(255),
  registrationNumber: z.string().max(100).optional().or(z.literal('')),
  addressLine1:       z.string().min(5, 'Address is required.').max(255),
  addressLine2:       z.string().max(255).optional().or(z.literal('')),
  city:               z.string().min(2, 'City is required.').max(100),
  state:              z.string().min(2, 'State is required.').max(100),
  pincode:            z.string().regex(/^[0-9]{6}$/, 'Enter a valid 6-digit pincode.'),
  country:            z.string().max(100).optional().or(z.literal('')),
  contactEmail:       z.string().email('Enter a valid email.').optional().or(z.literal('')),
  contactPhone:       z.string().max(20).optional().or(z.literal('')),
  websiteUrl:         z.string().max(500).optional().or(z.literal('')),
  totalUnits:         z.coerce.number().min(0).optional(),
  establishedYear:    z.coerce.number().min(1900).max(2100).optional(),
});

function ApartmentFormModal({ open, onClose, apartment }) {
  const isEdit = Boolean(apartment);
  const qc = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { country: 'India' },
  });

  useEffect(() => {
    if (open) {
      reset(apartment
        ? { ...apartment, totalUnits: apartment.totalUnits ?? 0, establishedYear: apartment.establishedYear ?? '' }
        : { country: 'India', totalUnits: 0 }
      );
    }
  }, [open, apartment, reset]);

  const mutation = useMutation({
    mutationFn: (data) => isEdit
      ? apartmentApi.update(apartment.id, data)
      : apartmentApi.create(data),
    onSuccess: () => {
      enqueueSnackbar(
        isEdit ? 'Apartment updated successfully.' : 'Apartment created successfully.',
        { variant: 'success' }
      );
      qc.invalidateQueries({ queryKey: ['apartments'] });
      qc.invalidateQueries({ queryKey: ['myApartment'] });
      onClose();
    },
    onError: (err) => {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    },
  });

  const F = (name, label, opts = {}) => (
    <TextField
      {...register(name)}
      label={label}
      fullWidth
      size="small"
      error={!!errors[name]}
      helperText={errors[name]?.message}
      {...opts}
    />
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth
            PaperProps={{ sx: { borderRadius: '20px' } }}>

      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
        <Typography variant="h6" fontWeight={700}>
          {isEdit ? 'Edit Apartment Society' : 'Add New Apartment Society'}
        </Typography>
        <IconButton size="small" onClick={onClose}><CloseOutlined fontSize="small" /></IconButton>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ pt: 3 }}>
        <Box component="form" id="apt-form" onSubmit={handleSubmit((d) => mutation.mutate(d))} noValidate>
          <Typography variant="subtitle2" color="text.secondary" mb={1.5} fontWeight={600}>
            BASIC INFORMATION
          </Typography>
          <Grid container spacing={2} mb={3}>
            <Grid item xs={12} md={8}>{F('name', 'Society Name *')}</Grid>
            <Grid item xs={12} md={4}>{F('registrationNumber', 'Registration Number')}</Grid>
            <Grid item xs={12} md={4}>{F('establishedYear', 'Established Year', { type: 'number' })}</Grid>
            <Grid item xs={12} md={4}>{F('totalUnits', 'Total Units', { type: 'number' })}</Grid>
            <Grid item xs={12} md={4}>{F('contactEmail', 'Contact Email', { type: 'email' })}</Grid>
            <Grid item xs={12} md={4}>{F('contactPhone', 'Contact Phone')}</Grid>
            <Grid item xs={12} md={4}>{F('websiteUrl', 'Website URL')}</Grid>
          </Grid>

          <Divider sx={{ mb: 2 }} />
          <Typography variant="subtitle2" color="text.secondary" mb={1.5} fontWeight={600}>
            ADDRESS
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12}>{F('addressLine1', 'Address Line 1 *')}</Grid>
            <Grid item xs={12}>{F('addressLine2', 'Address Line 2')}</Grid>
            <Grid item xs={12} md={4}>{F('city', 'City *')}</Grid>
            <Grid item xs={12} md={4}>{F('state', 'State *')}</Grid>
            <Grid item xs={6} md={2}>{F('pincode', 'Pincode *')}</Grid>
            <Grid item xs={6} md={2}>{F('country', 'Country')}</Grid>
          </Grid>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2, gap: 1 }}>
        <Button onClick={onClose} disabled={mutation.isPending} variant="outlined" sx={{ borderRadius: '10px' }}>
          Cancel
        </Button>
        <Button type="submit" form="apt-form" variant="contained" disabled={mutation.isPending}
                sx={{ borderRadius: '10px', minWidth: 120 }}
                startIcon={mutation.isPending ? <CircularProgress size={16} color="inherit" /> : null}>
          {mutation.isPending ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Society'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default ApartmentFormModal;
