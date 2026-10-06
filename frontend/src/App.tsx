import EventSeatIcon from "@mui/icons-material/EventSeat";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import { useState } from "react";
import { BookingList } from "./features/resources/BookingList";
import { ResourceTable } from "./features/resources/ResourceTable";

export default function App() {
  const [bookingRefreshKey, setBookingRefreshKey] = useState(0);

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <EventSeatIcon sx={{ mr: 1.5 }} />
          <Typography variant="h6" component="h1">
            Ressourcen-Buchung
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Stack spacing={3}>
          <Box>
            <Typography variant="h5" component="h2" gutterBottom>
              Buchbare Ressourcen
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Räume, Geräte und Fahrzeuge buchen und bestehende Termine verwalten.
            </Typography>
          </Box>

          <ResourceTable onBookingChanged={() => setBookingRefreshKey((key) => key + 1)} />
          <BookingList refreshKey={bookingRefreshKey} />
        </Stack>
      </Container>
    </Box>
  );
}
