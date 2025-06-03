import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function CreateScenesContent() {
    const [scenesName, setScenesName] = useState('');
    const [fromTime, setFromTime] = useState('');
    const [toTime, setToTime] = useState('');
    const [selectedDays, setSelectedDays] = useState([]);
    const [device, setDevice] = useState('');
    const [room, setRoom] = useState('');
    const [command, setCommand] = useState('ON');

    const [deviceList, setDeviceList] = useState([]);
    const [roomList, setRoomList] = useState([]);

    const navigate = useNavigate();

    const days = [
        { short: 'Sun', full: 'SUNDAY' },
        { short: 'Mon', full: 'MONDAY' },
        { short: 'Tue', full: 'TUESDAY' },
        { short: 'Wed', full: 'WEDNESDAY' },
        { short: 'Thu', full: 'THURSDAY' },
        { short: 'Fri', full: 'FRIDAY' },
        { short: 'Sat', full: 'SATURDAY' },
    ];

    useEffect(() => {
        axios.get('http://localhost:8081/user/room', { headers: authHeader() }).then(res => {
            console.log('Room data:', res.data);
            setRoomList(res.data);
        });
        axios.get('http://localhost:8081/user/device', { headers: authHeader() }).then(res => {
            setDeviceList(res.data.things || []);
        });

    }, []);

    console.log('Device list:', deviceList);

    const toggleDay = (dayFull) => {
        setSelectedDays(prev =>
            prev.includes(dayFull) ? prev.filter(d => d !== dayFull) : [...prev, dayFull]
        );
    };

    const handleSubmit = async () => {
        // Validate time format (HH:mm)
        const timeFormatRegex = /^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$/;
        if (!timeFormatRegex.test(fromTime)) {
            alert("Invalid fromTime format. Please use HH:mm (e.g., 15:16).");
            return;
        }
        if (!timeFormatRegex.test(toTime)) {
            alert("Invalid toTime format. Please use HH:mm (e.g., 15:16).");
            return;
        }

        // Existing validation for required fields
        if (!fromTime || !toTime || selectedDays.length === 0 || !room || !device || !command) {
            alert("All fields are required.");
            return;
        }

        const payload = {
            scenesName,
            fromTime,
            toTime,
            days: selectedDays,
            roomId: room,
            deviceId: device,
            command,
        };

        console.log(payload);
        

        try {
            const response = await axios.post('http://localhost:8081/user/scenes', payload, { headers: authHeader() });
            if (response.status === 200) {
                alert(response.data.message);
                navigate('/schedule/your_scenes');
            }
        } catch (err) {
            // Improved error handling to show specific backend error messages
            const errorMessage = err.response?.data?.error || 'Error submitting scene. Please try again.';
            alert(errorMessage);
        }
    };

    const authHeader = () => ({
        Authorization: `Bearer ${localStorage.getItem('token')}`,
    });

    return (
        <div className='container px-5 py-4'>
            <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Create Scenes</div>

            <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>
                <div className="mb-2">
                    <label className="form-label">Name</label>
                    <input type='text' className="form-control" value={scenesName} onChange={e => setScenesName(e.target.value)} required/>
                </div>

                <div className="mb-2">
                    <label className="form-label">Time</label>
                    <div className="d-flex gap-2">
                        <input type="time" className="form-control" value={fromTime} onChange={e => setFromTime(e.target.value)} required/>
                        <span className="align-self-center">To</span>
                        <input type="time" className="form-control" value={toTime} onChange={e => setToTime(e.target.value)} required/>
                    </div>
                </div>

                <div className="mb-2">
                    <label className="form-label">Days</label>
                    <div className="d-flex flex-wrap gap-2">
                        {days.map(({ short, full }) => (
                            <button
                                key={full}
                                className={`btn border border-dark rounded-circle d-flex justify-content-center align-items-center ${selectedDays.includes(full) ? 'btn-dark text-white' : 'btn-light'}`}
                                onClick={() => toggleDay(full)}
                                style={{ height: '35px', width: '35px', fontSize: '12px' }}>
                                {short}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="mb-2">
                    <label className="form-label">Device</label>
                    <select className="form-select" value={device} onChange={e => setDevice(e.target.value)} required>
                        <option value="">Select Device</option>
                        {deviceList.map(d => (
                            <option key={d.deviceId} value={d.deviceId}>{d.label}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-2">
                    <label className="form-label">Room</label>
                    <select className="form-select" value={room} onChange={e => setRoom(e.target.value)} required>
                        <option value="">Select Room</option>
                        {roomList.map(r => (
                            <option key={r.roomId} value={r.roomId}>{r.roomName}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-2">
                    <label className="d-block form-label">Condition</label>
                    <div className="btn-group border border-dark w-50">
                        <button className={`btn ${command === 'ON' ? 'btn-dark' : 'btn-light'}`} onClick={() => setCommand('ON')}>On</button>
                        <button className={`btn ${command === 'OFF' ? 'btn-dark' : 'btn-light'}`} onClick={() => setCommand('OFF')}>Off</button>
                    </div>
                </div>
            </div>

            <div className='text-end my-5'>
                <button className="btn btn-dark" onClick={handleSubmit}>Submit</button>
            </div>
        </div>
    );
};